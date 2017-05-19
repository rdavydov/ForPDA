package forpdateam.ru.forpda.fragments.news.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.news.Constants;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.news.callbacks.NewsListClickListener;
import forpdateam.ru.forpda.fragments.news.details.NewsDetailsFragment;
import forpdateam.ru.forpda.fragments.news.list.adapters.NewsListTopAdapter;
import forpdateam.ru.forpda.fragments.news.list.adapters.NewsListAdapter;
import forpdateam.ru.forpda.fragments.news.list.presenter.NewsPresenter;
import forpdateam.ru.forpda.models.news.NewsModel;
import forpdateam.ru.forpda.models.news.TopNewsModel;

import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by isanechek on 5/3/17.
 */

public class NewsListFragment extends TabFragment implements INewsView {
    private static final String TAG = "NewsListFragment";
    private static final int VISIBLE_THRESHOLD = 5;

    private TextView text;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private View srProgress;
    private int pageSize = 1;
    private boolean mIsLoading;
    private NewsPresenter presenter;
    private NewsListAdapter adapter;
    private NewsListTopAdapter topAdapter;

    public NewsListFragment(){
        configuration.setAlone(true);
        configuration.setUseCache(true);
        configuration.setDefaultTitle("Новости");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        log("Create News Fragment");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.news_list_fragment);
        log("Create View");
        srProgress = findViewById(R.id.news_list_progress);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.news_refresh_layout);
        refreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorAccent));
        recyclerView = (RecyclerView) findViewById(R.id.news_list);
        presenter = new NewsPresenter();
        presenter.bindView(this);

        refreshLayout.setOnRefreshListener(() -> presenter.updateData(Constants.NEWS_CATEGORY_ALL, false));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        log("View Created");
        setupRecyclerView(savedInstanceState);
        viewsReady();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        log("save instance");
        outState.putParcelable("scrollPosition", recyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onPause() {
        presenter.unbindView();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        presenter.unbindView();
        super.onDestroy();
    }

    @Override
    public void loadData() {
        presenter.loadData(Constants.NEWS_CATEGORY_ALL);
    }

    @Override
    public void showData(List<NewsModel> list) {
        msg("showData size " + list.size() + " <<");
        srProgress.setVisibility(View.GONE);
        refreshLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        adapter.addAll(list);

        Stream.of(list).forEach(new Consumer<NewsModel>() {
            @Override
            public void accept(NewsModel model) {
                log("T " + model.title + " N " + model.newNews);
            }
        });
    }

    @Override
    public void showTopCommentsNews(List<TopNewsModel> list) {
        if (adapter.getHeadersCount() == 0) {
            adapter.addHeader(inflateHeaderFooter(list));
        }
    }

    @Override
    public void updateTopCommentsNews(TopNewsModel model) {
        if (topAdapter != null) {
            log("top adapter not null");
            topAdapter.add(0, model);
        }
    }
    @Override
    public void updateDataList(List<NewsModel> list) {
        adapter.addAll(0, list);
        log("updateDataList size " + list.size());
    }

    @Override
    public void updateLoadMore(List<NewsModel> list) {
        if (list == null) {
            // show progress
        } else if (list.size() > 0){
            // remove progress
            adapter.addAll(list.size() + 1, list);
        }
    }

    @Override
    public void showLoadMore(List<NewsModel> list) {
        log("showLoadMore");
        adapter.removeChild(adapter.getRealItemCount() - 1);
        adapter.addAll(adapter.getRealItemCount() - 1, list);
    }

    @Override
    public void showErrorLoadMore() {

    }

    @Override
    public void showUpdateProgress(boolean show) {
        refreshLayout.setEnabled(show);
        log("showUpdateProgress " + show);
    }

    @Override
    public void showBackgroundWorkProgress(boolean show) {

    }

    @Override
    public void showErrorView(Throwable throwable, @NonNull String codeError) {

    }

    @Override
    public void showPopUp(@NonNull String viewCode) {
        Toast.makeText(getActivity(), viewCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showToast(@NonNull String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void setupRecyclerView(Bundle savedInstanceState) {
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemViewCacheSize(30);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new NewsListAdapter();

        recyclerView.setAdapter(adapter);
        if (savedInstanceState != null) {
            log("savedInstanceState true");
//            recyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable("scrollPosition"));
//            presenter.reInstance(Constants.NEWS_CATEGORY_ALL, adapter.getItemCount());
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = manager.getItemCount();
                int lastVisibleItem = manager.findLastVisibleItemPosition();
                if (!mIsLoading && totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {
                    mIsLoading = true;
                    pageSize++;
                    adapter.add(adapter.getRealItemCount() - 1, null);
                    presenter.loadMore(Constants.NEWS_CATEGORY_ALL, pageSize);
                    log("Load More");
                }
            }
        });

        adapter.setActionListener(new NewsListClickListener() {
            @Override
            public void click(View view, int position) {
                log("CLICK");
                NewsModel model = adapter.getItem(position);
                Bundle args = new Bundle();
                args.putString(NewsDetailsFragment.TITLE, model.title);
                args.putString(NewsDetailsFragment.IMG_URL, model.imgLink);
                args.putString(NewsDetailsFragment.NEWS_URL, model.link);
                TabManager.getInstance().add(new TabFragment.Builder<>(NewsDetailsFragment.class).setArgs(args).build());
            }

            @Override
            public void longClick(int position) {
                log("LONG CLICK");
            }
        });
    }

    private View inflateHeaderFooter(List<TopNewsModel> list) {
        msg("inflateHeaderFooter here");
        LinearLayout header = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.news_list_item_header, recyclerView, false);
        RecyclerView topList = (RecyclerView) header.findViewById(R.id.news_list_top_list);
        topList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        topList.setHasFixedSize(true);
        topAdapter = new NewsListTopAdapter();
        if (list != null) { topAdapter.addAll(list); }
        topList.setAdapter(topAdapter);
        msg("Top adapter size " + topAdapter.getItemCount());
        return header;
    }

    private void msg(String s) {
        Log.e("TEST", s);
    }
}
