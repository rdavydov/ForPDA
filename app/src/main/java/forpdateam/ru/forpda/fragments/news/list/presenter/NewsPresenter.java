package forpdateam.ru.forpda.fragments.news.list.presenter;

import android.support.annotation.NonNull;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.fragments.news.list.INewsView;
import forpdateam.ru.forpda.models.news.NewsModel;
import forpdateam.ru.forpda.models.news.TopNewsModel;
import forpdateam.ru.forpda.realm.RealmMapping;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by isanechek on 12.01.17.
 */

public class NewsPresenter implements INewsPresenter {
    private static final String TAG = "NewsPresenter";

    private CompositeDisposable disposable;
    private INewsView newsView;

    private Realm mRealm;

    @Override
    public void bindView(INewsView iNewsView) {
        this.newsView = iNewsView;
        mRealm = Realm.getInstance(App.getInstance().getNewsRealmConfig());
        disposable = new CompositeDisposable();
        log(TAG + " bindView");
    }

    @Override
    public void unbindView() {
        newsView = null;
        disposable.dispose();
        log(TAG + " unbindView");
    }

    /**
     * Основной метод для показа данных из сети или базы.
     * @param category - категория новостей.
     */
    @Override
    public void loadData(String category) {
        log("Load Data");
        List<NewsModel> cache = mRealm.where(NewsModel.class).equalTo(NewsModel.CATEGORY, category).findAll();
        if (!cache.isEmpty()) {
            log("load data from cache");
            findTopCommentsModel(cache);
            newsView.showData(cache);

        } else {
            log("load data from network");
            newsView.showUpdateProgress(true);
            disposable.add(Api.NewsList().getNewsListFromNetwork1(category, 0)
                    .subscribeOn(Schedulers.io())
                    .map(RealmMapping::getMappingUpdateNewsList)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(list -> {
                        log("load data from network size " + list.size());
                        mRealm.executeTransaction(realm -> realm.insertOrUpdate(list));
                        findTopCommentsModel(cache);
                        newsView.showUpdateProgress(false);
                        newsView.showData(list);
                    }));
        }
    }

    private void findTopCommentsModel(List<NewsModel> list) {
        NewsModel topModel = RealmMapping.getTopModel(list);
        TopNewsModel model = new TopNewsModel();
        model.link = topModel.link;
        model.imgUrl = topModel.imgLink;
        model.title = topModel.title;
        model.commentsCount = topModel.commentsCount;
        List<TopNewsModel> models = new ArrayList<>();
        models.add(model);
        newsView.showTopCommentsNews(models);
        mRealm.executeTransaction(realm -> realm.insertOrUpdate(model));
    }


    @Override
    public void loadTopCommentsNews() {
        log("Presenter To Comments");
        List<TopNewsModel> cache = mRealm.where(TopNewsModel.class).findAll();
        if (!cache.isEmpty()) {
            log("Top Comments Size " + cache.size());
            newsView.showTopCommentsNews(cache);
        } else {
            log("loadTopCommentsNews NULL");

        }
    }

    /**
     * Основной метод обноления списка и бд.
     * @param category - категория новостей.
     */
    @Override
    public void updateData(@NonNull String category, boolean background) {
        log(TAG + " updateNewsListData -> " + " category -> " + category);
//        if (background) {
//            newsView.showBackgroundWorkProgress(true);
//        }
//        disposable.add(mNewsRepository.updateNewsListData(category)
//                .subscribe(newsCallbackModel -> {
//                    log(TAG + " updateData " + newsCallbackModel.cache.size());
//                    if (background) { newsView.showBackgroundWorkProgress(false);}
//                    else { newsView.showUpdateProgress(false);}
//                    newsView.updateDataList(newsCallbackModel);
//                }, throwable -> {
//                    log(TAG + " updateData Error ->> " + throwable.toString());
//                    if (background) { newsView.showBackgroundWorkProgress(false);}
//                    else { newsView.showUpdateProgress(false);}
//                    newsView.showErrorView(throwable, ERROR_UPDATE_DATA);
//                }));

        newsView.showUpdateProgress(true);
        disposable.add(Api.NewsList().getNewsListFromNetwork1(category, 0)
                .subscribeOn(Schedulers.io())
                .map(RealmMapping::getMappingUpdateNewsList)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    List<NewsModel> cache = checkList(list, mRealm.where(NewsModel.class).equalTo("category", category).findAll());
                    int size = cache.size();
                    if (size > 0) {
                        newsView.showUpdateProgress(false);
                        newsView.updateDataList(cache);
                        newsView.showPopUp(size + " новых новостей");
                        mRealm.executeTransaction(realm -> realm.insertOrUpdate(cache));
                    } else {
                        newsView.showPopUp("Нет новых новостей");
                    }
                }));
    }

    private List<NewsModel> checkList(List<NewsModel> list, List<NewsModel> cache2) {
        List<NewsModel> cache = new ArrayList<>();
        Stream.of(list).filterNot(newModel -> Stream.of(cache2)
                .anyMatch(oldModel -> newModel.link.equals(oldModel.link)))
                .forEach(cache::add);
        return cache;
    }

    @Override
    public void reInstance(@NonNull String category, int size) {
        if (size > 0) { updateData(category, true); }
        else { loadData(category); }
    }

    /**
     *
     * @param category
     * @param pageNumber
     * @param lastUrl
     */
    @Override
    public void loadMoreNewItems(@NonNull String category, int pageNumber, String lastUrl) {
//        log(TAG + " LoadMoreNewItems");
//        disposable.add(mNewsRepository.getLoadMoreNewsListData(category, pageNumber, lastUrl)
//                .subscribe(newsCallbackModel -> {
////                    newsView.showMoreNewNews(newsCallbackModel);
//                }, throwable -> {
//
//                    newsView.showErrorView(throwable, ERROR_LOAD_MORE_NEW_DATA);
//                }));
    }

    /**
     *
     * @param category
     * @param pageNumber
     */
    @Override
    public void loadMore(@NonNull String category, int pageNumber) {
//        disposable.add(mNewsRepository.loadMoreNewsItems(category, pageNumber)
//                .subscribe(list -> newsView.showLoadMore(list),
//                        new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Exception {
//
//                    }
//                }));
    }


}
