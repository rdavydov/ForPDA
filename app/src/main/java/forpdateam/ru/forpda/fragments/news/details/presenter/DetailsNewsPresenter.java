package forpdateam.ru.forpda.fragments.news.details.presenter;

import android.support.annotation.NonNull;

import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.news.NewsParser;
import forpdateam.ru.forpda.fragments.news.details.NDView;
import forpdateam.ru.forpda.fragments.news.details.NewsDetailsParser;
import forpdateam.ru.forpda.models.news.NewsDetailsModel;
import forpdateam.ru.forpda.models.news.NewsModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by isanechek on 5/8/17.
 */

public class DetailsNewsPresenter implements IDetailNewsPresenter {
    private CompositeDisposable disposable;
    private NDView mView;
    private Realm mRealm;
    private Map<String, String> cache = new HashMap<>();

    @Override
    public void bindView(NDView view) {
        this.mView = view;
        disposable = new CompositeDisposable();
        mRealm = Realm.getInstance(App.getInstance().getNewsRealmConfig());
        log("DetailsNewsPresenter BIND");
    }

    @Override
    public void unbindView() {
        if (cache.isEmpty()) {
            cache.clear();
        }
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Override
    public void loadModel(@NonNull String id) {
        NewsDetailsModel detailsModel = mRealm.where(NewsDetailsModel.class).equalTo("link", id).findFirst();
        if (detailsModel == null) {
            log("Details loadModel News Details Model Null");
            NewsModel newsModel = mRealm.where(NewsModel.class).equalTo("link", id).findFirst();
//            mView.showTopHeader(newsModel.commentsCount, newsModel.date, newsModel.author, newsModel.tagList);
        } else {
            log("Details loadModel News Details Model Not Null");
//            mView.showTopHeader(detailsModel.commentsCount, detailsModel.date, detailsModel.author, detailsModel.tagList);
        }
    }

    @Override
    public void loadContentData(@NonNull String url) {
        log("Details loadContentData -->> " + url);
        loadModel(url);
        disposable.add(NewsParser.getDetailsSingle(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    log("Details loadData Response " + s);
//                    cache.put("source", s);
                    Element page = NewsDetailsParser.getRootElement(s);
                    mView.showData(NewsDetailsParser.getContent(page));
                    mView.showMoreNews(NewsDetailsParser.getMoreNews(page));
                    mView.showNextPrevPage(NewsDetailsParser.getNextPrevNewsLink(page));
                    mView.showComments(NewsDetailsParser.getComments(page));
                }, throwable -> log("Details loadContentData Error " + throwable.getMessage())));
    }
}
