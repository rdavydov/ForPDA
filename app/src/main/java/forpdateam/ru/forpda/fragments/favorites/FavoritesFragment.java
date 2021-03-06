package forpdateam.ru.forpda.fragments.favorites;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.api.favorites.interfaces.IFavItem;
import forpdateam.ru.forpda.api.favorites.models.FavData;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import forpdateam.ru.forpda.bdobjects.favorites.FavItemBd;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.forum.ForumHelper;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.settings.Preferences;
import forpdateam.ru.forpda.utils.AlertDialogMenu;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.Utils;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.pagination.PaginationHelper;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by radiationx on 22.09.16.
 */

public class FavoritesFragment extends TabFragment {
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private FavoritesAdapter.OnItemClickListener onItemClickListener =
            favItem -> {
                Bundle args = new Bundle();
                args.putString(TabFragment.ARG_TITLE, favItem.getTopicTitle());
                if(favItem.isForum()){
                    IntentHandler.handle("http://4pda.ru/forum/index.php?showforum=" + favItem.getForumId(), args);
                }else {
                    IntentHandler.handle("http://4pda.ru/forum/index.php?showtopic=" + favItem.getTopicId() + "&view=getnewpost", args);
                }
            };
    private AlertDialogMenu<FavoritesFragment, IFavItem> favoriteDialogMenu, showedFavoriteDialogMenu;
    private FavoritesAdapter.OnItemClickListener onLongItemClickListener =
            favItem -> {
                if (favoriteDialogMenu == null) {
                    favoriteDialogMenu = new AlertDialogMenu<>();
                    showedFavoriteDialogMenu = new AlertDialogMenu<>();
                    favoriteDialogMenu.addItem("Скопировать ссылку", (context, data) ->{
                        if(data.isForum()){
                            Utils.copyToClipBoard("http://4pda.ru/forum/index.php?showforum=".concat(Integer.toString(data.getForumId())));
                        }else {
                            Utils.copyToClipBoard("http://4pda.ru/forum/index.php?showtopic=".concat(Integer.toString(data.getTopicId())));
                        }
                    } );
                    favoriteDialogMenu.addItem("Вложения", (context, data) -> IntentHandler.handle("http://4pda.ru/forum/index.php?act=attach&code=showtopic&tid=" + data.getTopicId()));
                    favoriteDialogMenu.addItem("Открыть форум темы", (context, data) -> IntentHandler.handle("http://4pda.ru/forum/index.php?showforum=" + data.getForumId()));
                    favoriteDialogMenu.addItem("Изменить тип подписки", (context, data) -> {
                        new AlertDialog.Builder(context.getContext())
                                .setItems(Favorites.SUB_NAMES, (dialog1, which1) -> context.changeFav(Favorites.ACTION_EDIT_SUB_TYPE, Favorites.SUB_TYPES[which1], data.getFavId()))
                                .show();
                    });
                    favoriteDialogMenu.addItem(getPinText(favItem.isPin()), (context, data) -> context.changeFav(Favorites.ACTION_EDIT_PIN_STATE, data.isPin() ? "unpin" : "pin", data.getFavId()));
                    favoriteDialogMenu.addItem("Удалить", (context, data) -> context.changeFav(Favorites.ACTION_DELETE, null, data.getFavId()));
                }
                showedFavoriteDialogMenu.clear();

                showedFavoriteDialogMenu.addItem(favoriteDialogMenu.get(0));
                if (!favItem.isForum()){
                    showedFavoriteDialogMenu.addItem(favoriteDialogMenu.get(1));
                    showedFavoriteDialogMenu.addItem(favoriteDialogMenu.get(2));
                }
                showedFavoriteDialogMenu.addItem(favoriteDialogMenu.get(3));
                showedFavoriteDialogMenu.addItem(favoriteDialogMenu.get(4));
                showedFavoriteDialogMenu.addItem(favoriteDialogMenu.get(5));

                int index = showedFavoriteDialogMenu.containsIndex(getPinText(!favItem.isPin()));
                if (index != -1)
                    showedFavoriteDialogMenu.changeTitle(index, getPinText(favItem.isPin()));

                new AlertDialog.Builder(getContext())
                        .setItems(showedFavoriteDialogMenu.getTitles(), (dialog, which) -> {
                            Log.d("FORPDA_LOG", "ocnlicl " + favItem + " : " + favItem.getFavId());
                            showedFavoriteDialogMenu.onClick(which, FavoritesFragment.this, favItem);
                        })
                        .show();
            };

    private Realm realm;
    private RealmResults<FavItemBd> results;
    private FavoritesAdapter adapter;
    private Subscriber<FavData> mainSubscriber = new Subscriber<>(this);
    boolean markedRead = false;

    private boolean unreadTop = App.getInstance().getPreferences().getBoolean("lists.topic.unread_top", false);
    private Observer favoritesPreferenceObserver = (observable, o) -> {
        if (o == null) return;
        String key = (String) o;
        switch (key) {
            case Preferences.Favorites.UNREAD_TOP: {
                boolean newUnreadTop = App.getInstance().getPreferences().getBoolean(Preferences.Favorites.UNREAD_TOP, false);
                if (newUnreadTop != unreadTop) {
                    unreadTop = newUnreadTop;
                    bindView();
                }
                break;
            }
            case Preferences.Favorites.SHOW_DOT: {
                boolean newShowDot = App.getInstance().getPreferences().getBoolean(Preferences.Favorites.SHOW_DOT, false);
                if (newShowDot != adapter.isShowDot()) {
                    adapter.setShowDot(newShowDot);
                    adapter.notifyDataSetChanged();
                }
                break;
            }
        }
    };

    public FavoritesFragment() {
        configuration.setAlone(true);
        //configuration.setUseCache(true);
        configuration.setDefaultTitle("Избранное");
    }

    private CharSequence getPinText(boolean b) {
        return b ? "Открепить" : "Закрепить";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    private PaginationHelper paginationHelper = new PaginationHelper();
    private int currentSt = 0;

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
        //recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        adapter = new FavoritesAdapter();
        adapter.setOnItemClickListener(onItemClickListener);
        adapter.setOnLongItemClickListener(onLongItemClickListener);
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
                currentSt = pageNumber;
                loadData();
            }
        });

        bindView();
        App.getInstance().addPreferenceChangeObserver(favoritesPreferenceObserver);
        return view;
    }

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        getMenu().add("Отметить все прочитанными")
                .setOnMenuItemClickListener(item -> {
                    new AlertDialog.Builder(getContext())
                            .setMessage("Отметить все прочитанными?")
                            .setPositiveButton("Да", (dialog, which) -> {
                                ForumHelper.markAllRead(o -> {
                                    loadData();
                                });
                            })
                            .setNegativeButton("Нет", null)
                            .show();
                    return false;
                });
    }

    @Override
    public void loadData() {
        refreshLayout.setRefreshing(true);
        mainSubscriber.subscribe(RxApi.Favorites().getFavorites(currentSt), this::onLoadThemes, new FavData(), v -> loadData());
    }

    private void onLoadThemes(FavData data) {
        Log.d("FORPDA_LOG", "loaded itms " + data.getItems().size() + " : " + results.size());
        refreshLayout.setRefreshing(false);
        recyclerView.scrollToPosition(0);
        if (data.getItems().size() == 0)
            return;

        realm.executeTransactionAsync(r -> {
            r.delete(FavItemBd.class);
            List<FavItemBd> bdList = new ArrayList<>();
            for (FavItem item : data.getItems()) {
                bdList.add(new FavItemBd(item));
            }
            r.copyToRealmOrUpdate(bdList);
            bdList.clear();
        }, this::bindView);
        paginationHelper.updatePagination(data.getPagination());
        setSubtitle(paginationHelper.getString());
    }

    private void bindView() {
        results = realm.where(FavItemBd.class).findAll();
        if (results.size() != 0) {
            ArrayList<IFavItem> nonBdResult = new ArrayList<>();
            for (FavItemBd itemBd : results) {
                nonBdResult.add(new FavItem(itemBd));
            }
            ArrayList<IFavItem> pinnedUnread = new ArrayList<>();
            ArrayList<IFavItem> itemsUnread = new ArrayList<>();
            ArrayList<IFavItem> pinned = new ArrayList<>();
            ArrayList<IFavItem> items = new ArrayList<>();
            for (IFavItem item : nonBdResult) {
                if (item.isPin()) {
                    if (unreadTop && item.isNewMessages()) {
                        pinnedUnread.add(item);
                    } else {
                        pinned.add(item);
                    }
                } else {
                    if (unreadTop && item.isNewMessages()) {
                        itemsUnread.add(item);
                    } else {
                        items.add(item);
                    }
                }
            }


            adapter.clear();
            if (pinnedUnread.size() > 0) {
                Log.e("FORPDA_LOG", "ADD UNREAD PINNED " + pinnedUnread.size());
                adapter.addSection(new Pair<>("Непрочитанные закрепленные темы", pinnedUnread));
            }
            if (itemsUnread.size() > 0) {
                Log.e("FORPDA_LOG", "ADD UNREAD ITEMs " + itemsUnread.size());
                adapter.addSection(new Pair<>("Непрочитанные темы", itemsUnread));
            }
            if (pinned.size() > 0) {
                Log.e("FORPDA_LOG", "ADD PINNED " + pinned.size());
                adapter.addSection(new Pair<>("Закрепленные темы", pinned));
            }
            Log.e("FORPDA_LOG", "ADD ITEMS " + items.size());
            adapter.addSection(new Pair<>("Темы", items));
            adapter.notifyDataSetChanged();
        }
        if (!Client.getInstance().getNetworkState()) {
            ClientHelper.getInstance().notifyCountsChanged();
        }
    }

    public void changeFav(int action, String type, int favId) {
        FavoritesHelper.changeFav(this::onChangeFav, action, favId, -1, type);
    }

    public void markRead(int topicId) {
        realm.executeTransactionAsync(realm1 -> {
            IFavItem favItem = realm1.where(FavItemBd.class).equalTo("topicId", topicId).findFirst();
            if (favItem != null) {
                favItem.setNewMessages(false);
            }
        });
        markedRead = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (markedRead) {
            markedRead = false;
            bindView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getInstance().removePreferenceChangeObserver(favoritesPreferenceObserver);
        realm.close();
    }

    private void onChangeFav(boolean v) {
        /*if (!v)
            Toast.makeText(getContext(), "При выполнении операции произошла ошибка", Toast.LENGTH_SHORT).show();*/
        Toast.makeText(getContext(), "Действие выполнено", Toast.LENGTH_SHORT).show();
        loadData();
    }
}
