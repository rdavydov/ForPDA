package forpdateam.ru.forpda.fragments.news.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.news.details.presenter.DetailsNewsPresenter;
import forpdateam.ru.forpda.models.news.MoreNewsModel;
import forpdateam.ru.forpda.models.news.NCModel;
import forpdateam.ru.forpda.models.news.NewsCommentModel;
import forpdateam.ru.forpda.models.news.TagModel;
import io.realm.RealmList;

import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by isanechek on 5/4/17.
 */

public class NewsDetailsFragment extends TabFragment implements NDView {

    public static final String TITLE = "nd.title";
    public static final String IMG_URL = "nd.img.url";
    public static final String NEWS_URL = "nd.news.url";
    private String url;
    private String imgUrl;
    private String title;
    private RecyclerView mRView;
    private WebView webView;
    private LinearLayout root;
    private NDAdapter mAdapter;
    private DetailsNewsPresenter mPresenter;


    private void msg(String s) {
        Log.e("VALERA", s);
    }

    public NewsDetailsFragment() {
        configuration.setAlone(true);
        configuration.setUseCache(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        msg("Пивет");
        if (getArguments() != null) {
            title = getArguments().getString(TITLE);
//            configuration.setDefaultTitle(title);
            url = getArguments().getString(NEWS_URL);
            url = "http:" + url;
            imgUrl = getArguments().getString(IMG_URL);
            imgUrl = "http:" + imgUrl;
            msg("title --> " + title);
            msg("url --> " + url);
            msg("img url --> " + imgUrl);
            mPresenter = new DetailsNewsPresenter();
        } else {
            msg("Нет аргумента, хозяин");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getContext().getTheme().applyStyle(R.style.AppTheme_NoActionBar, true);
        baseInflateFragment(inflater, R.layout.news_details_fragment);
        msg("OnCreateView");
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(title);
        mRView = (RecyclerView) findViewById(R.id.recycler_view);
        root = (LinearLayout) findViewById(R.id.web_container);
        webView = (WebView) findViewById(R.id.nd_content_webview);
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        ImageLoader.getInstance().displayImage(imgUrl, imageView);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        msg("onViewCreated");
        mRView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRView.setNestedScrollingEnabled(false);
        mAdapter = new NDAdapter();
        mRView.setAdapter(mAdapter);
        log("boom");
        mPresenter.bindView(this);


        viewsReady();
    }

    @Override
    public void loadData() {
        super.loadData();
        msg("Data load");
        mPresenter.loadContentData(url);

    }

    @Override
    public void showTopHeader(String commentsCount, String date, String author, RealmList<TagModel> tagList) {

        log("showTopHeader CC " + commentsCount);
        log("showTopHeader D " + date);
        log("showTopHeader A " + author);

    }

    @Override
    public void showData(String html) {
//        log("showData " + html);
//        View header = getActivity().getLayoutInflater().inflate(R.layout.news_details_web_item, mRView, false);
//        LinearLayout root = (LinearLayout) header.findViewById(R.id.nd_rootweb_header);
//        WebView webView = (WebView) header.findViewById(R.id.nd_web_item);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                root.requestLayout();
            }
        });
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
//        mAdapter.addHeader(header);


    }

    @Override
    public void showMoreNews(List<MoreNewsModel> list) {
        log("showMoreNews S " + list.size());
        Stream.of(list).forEach(new com.annimon.stream.function.Consumer<MoreNewsModel>() {
            @Override
            public void accept(MoreNewsModel moreNewsModel) {
                log("showMoreNews title " + moreNewsModel.title);
            }
        });
    }

    @Override
    public void showNextPrevPage(List<String> list) {
        Stream.of(list).forEach(new com.annimon.stream.function.Consumer<String>() {
            @Override
            public void accept(String s) {
                log("showNextPrevPage " + s);

            }
        });
    }

    @Override
    public void showComments(List<NCModel> list) {
        log("showComments S " + list.size());
        List<NewsCommentModel> comments = new ArrayList<>();
        Stream.of(list)
                .map(new Function<NCModel, NewsCommentModel>() {
                    @Override
                    public NewsCommentModel apply(NCModel ncModel) {
                        NewsCommentModel commentModel = new NewsCommentModel();
                        commentModel.comment = ncModel.text;
                        return commentModel;
                    }
                }).forEach(new com.annimon.stream.function.Consumer<NewsCommentModel>() {
            @Override
            public void accept(NewsCommentModel newsCommentModel) {
                comments.add(newsCommentModel);
            }
        });

        mAdapter.addAll(comments);


    }
}
