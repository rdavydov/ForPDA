package forpdateam.ru.forpda.fragments.reputation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.reputation.Reputation;
import forpdateam.ru.forpda.api.reputation.models.RepData;
import forpdateam.ru.forpda.api.reputation.models.RepItem;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.pagination.PaginationHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 20.03.17.
 */

public class ReputationFragment extends TabFragment {

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private ReputationAdapter adapter;
    private Subscriber<RepData> mainSubscriber = new Subscriber<>(this);
    private PaginationHelper paginationHelper = new PaginationHelper();
    private RepData data = new RepData();
    private static AlertDialogMenu<ReputationFragment, RepItem> repDialogMenu;
    private AlertDialogMenu<ReputationFragment, RepItem> showedRepDialogMenu;


    public ReputationFragment() {
        configuration.setDefaultTitle("Репутация");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String url = getArguments().getString(ARG_TAB);
            if (url != null) {
                data = Reputation.fromUrl(data, url);
            }
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setListsBackground();
        baseInflateFragment(inflater, R.layout.fragment_base_list);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list);
        recyclerView = (RecyclerView) findViewById(R.id.base_list);
        viewsReady();
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setOnRefreshListener(this::loadData);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ReputationAdapter();
        recyclerView.setAdapter(adapter);


        paginationHelper.inflatePagination(getContext(), inflater, toolbar);
        paginationHelper.setupToolbar(toolbarLayout);
        paginationHelper.setListener(new PaginationHelper.PaginationListener() {
            @Override
            public boolean onTabSelected(TabLayout.Tab tab) {
                return refreshLayout.isRefreshing();
            }

            @Override
            public void onSelectedPage(int pageNumber) {
                data.getPagination().setSt(pageNumber);
                loadData();
            }
        });

        adapter.setOnItemClickListener(this::someClick);
        adapter.setOnLongItemClickListener(this::someClick);

        return view;
    }

    private void someClick(RepItem item) {
        if (repDialogMenu == null) {
            repDialogMenu = new AlertDialogMenu<>();
            repDialogMenu.addItem("Профиль", (context, data1) -> {
                IntentHandler.handle("http://4pda.ru/forum/index.php?showuser=" + data1.getUserId());
            });
            repDialogMenu.addItem("Перейти к сообщению", (context, data1) -> {
                IntentHandler.handle(data1.getSourceUrl());
            });
        }
        if (showedRepDialogMenu == null)
            showedRepDialogMenu = new AlertDialogMenu<>();

        showedRepDialogMenu.clear();
        showedRepDialogMenu.addItem(repDialogMenu.get(0));
        if (item.getSourceUrl() != null)
            showedRepDialogMenu.addItem(repDialogMenu.get(1));

        new AlertDialog.Builder(getContext())
                .setTitle(item.getUserNick())
                .setItems(showedRepDialogMenu.getTitles(), (dialog, which) -> showedRepDialogMenu.onClick(which, ReputationFragment.this, item))
                .show();
    }

    private MenuItem descSortMenuItem;
    private MenuItem ascSortMenuItem;
    private MenuItem repModeMenuItem;
    private MenuItem upRepMenuItem;
    private MenuItem downRepMenuItem;

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        SubMenu subMenu = getMenu().addSubMenu("Сортировка");
        subMenu.getItem().setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        subMenu.getItem().setIcon(App.getAppDrawable(getContext(), R.drawable.ic_toolbar_sort));
        descSortMenuItem = subMenu.add("По убыванию").setOnMenuItemClickListener(menuItem -> {
            data.setSort(Reputation.SORT_DESC);
            loadData();
            return false;
        });
        ascSortMenuItem = subMenu.add("По возрастанию").setOnMenuItemClickListener(menuItem -> {
            data.setSort(Reputation.SORT_ASC);
            loadData();
            return false;
        });
        repModeMenuItem = getMenu().add(data.getMode().equals(Reputation.MODE_FROM) ? "Репутация пользователя" : "Кому изменял")
                .setOnMenuItemClickListener(item -> {
                    if (data.getMode().equals(Reputation.MODE_FROM))
                        data.setMode(Reputation.MODE_TO);
                    else
                        data.setMode(Reputation.MODE_FROM);
                    loadData();
                    return false;
                });
        upRepMenuItem = getMenu().add("Повысить")
                .setOnMenuItemClickListener(item -> {
                    changeReputation(true);
                    return false;
                });
        downRepMenuItem = getMenu().add("Понизить")
                .setOnMenuItemClickListener(item -> {
                    changeReputation(false);
                    return false;
                });
        refreshToolbarMenuItems(false);
    }

    @Override
    protected void refreshToolbarMenuItems(boolean enable) {
        super.refreshToolbarMenuItems(enable);
        if (enable) {
            descSortMenuItem.setEnabled(true);
            ascSortMenuItem.setEnabled(true);
            repModeMenuItem.setEnabled(true);
            repModeMenuItem.setTitle(data.getMode().equals(Reputation.MODE_FROM) ? "Репутация пользователя" : "Кому изменял");
            if (data.getId() != ClientHelper.getUserId()) {
                upRepMenuItem.setEnabled(true);
                upRepMenuItem.setVisible(true);
                downRepMenuItem.setEnabled(true);
                downRepMenuItem.setVisible(true);
            }
        } else {
            descSortMenuItem.setEnabled(false);
            ascSortMenuItem.setEnabled(false);
            repModeMenuItem.setEnabled(false);
            upRepMenuItem.setEnabled(false);
            upRepMenuItem.setEnabled(false);
            upRepMenuItem.setVisible(false);
            downRepMenuItem.setVisible(false);
        }
    }

    public void changeReputation(boolean type) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.reputation_change_layout, null);

        assert layout != null;
        final TextView text = (TextView) layout.findViewById(R.id.reputation_text);
        final EditText messageField = (EditText) layout.findViewById(R.id.reputation_text_field);
        text.setText((type ? "Повысить" : "Понизить").concat(" репутацию ").concat(data.getNick()).concat(" ?"));

        new AlertDialog.Builder(getContext())
                .setView(layout)
                .setPositiveButton("Да", (dialogInterface, i) -> {
                    changeReputation(s -> {
                        Toast.makeText(getContext(), s.isEmpty() ? "Репутация изменена" : s, Toast.LENGTH_SHORT).show();
                        loadData();
                    }, 0, data.getId(), type, messageField.getText().toString());
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    public void changeReputation(@NonNull Consumer<String> onNext, int postId, int userId, boolean type, String message) {
        RxApi.Reputation().editReputation(postId, userId, type, message).onErrorReturn(throwable -> "error")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext);
    }

    @Override
    public void loadData() {
        refreshLayout.setRefreshing(true);
        refreshToolbarMenuItems(false);
        mainSubscriber.subscribe(RxApi.Reputation().getReputation(data), this::onLoadThemes, data, v -> loadData());
    }

    private void onLoadThemes(RepData data) {
        refreshLayout.setRefreshing(false);
        recyclerView.scrollToPosition(0);
        this.data = data;


        adapter.addAll(data.getItems());
        paginationHelper.updatePagination(data.getPagination());
        refreshToolbarMenuItems(true);
        //setSubtitle(paginationHelper.getString());
        setSubtitle("" + (data.getPositive() - data.getNegative()) + " (+" + data.getPositive() + " / -" + data.getNegative() + ")");
        setTabTitle("Репутация " + data.getNick() + (data.getMode().equals(Reputation.MODE_FROM) ? ": кому изменял" : ""));
        setTitle("Репутация " + data.getNick() + (data.getMode().equals(Reputation.MODE_FROM) ? ": кому изменял" : ""));
    }


}
