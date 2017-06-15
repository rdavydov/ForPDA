package forpdateam.ru.forpda.fragments.news.list.presenter;

import android.support.annotation.NonNull;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.fragments.news.list.INewsView;
import forpdateam.ru.forpda.models.news.NewsModel;
import forpdateam.ru.forpda.models.news.TopNewsModel;
import forpdateam.ru.forpda.pref.Preferences;
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
    private INewsView view;
    private Realm mRealm;
    private static final long MINIMUM_UPDATE_WAIT = 1800000; // 30 min
    private static final long MINIMUM_WAIT_NETWORK_REQUEST = 120; // 2 min
    private Map<String, Long> timeWaitUpdate = new HashMap<>();
    private int pageNumber = 2;

    @Override
    public void bindView(INewsView iNewsView) {
        this.view = iNewsView;
        mRealm = Realm.getInstance(App.getInstance().getNewsRealmConfig());
        disposable = new CompositeDisposable();
    }

    @Override
    public void unbindView() {
        disposable.dispose();
    }

    /**
     * Основной метод для показа данных из сети или базы.
     * @param category - категория новостей.
     */
    @Override
    public void loadData(String category) {
        log("Load Data");
        List<NewsModel> cache = mRealm.where(NewsModel.class).equalTo(NewsModel.CATEGORY, category).findAllAsync();
        if (!cache.isEmpty()) {
            log("load data from cache");
            view.showData(cache);
            showTopComments(cache, category);
            updateData(category, true);

        } else {
            log("load data from network");
            view.showUpdateProgress(true);
            disposable.add(Api.NewsApi().getNewsListFromNetwork1(category, 0)
                    .subscribeOn(Schedulers.io())
                    .map(RealmMapping::getMappingUpdateNewsList)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(list -> {
                        log("load data from network size " + list.size());
                        view.showUpdateProgress(false);
                        view.showData(list);
                        showTopComments(list, category);
                        mRealm.executeTransaction(realm -> realm.insertOrUpdate(list));
                    }, throwable -> {
                        view.showUpdateProgress(false);
                        view.showToast("Не могу загрузить.");
                    }));
        }
    }

    /**
     * Основной метод обноления списка и бд.
     * @param category - категория новостей.
     */
    @Override
    public void updateData(@NonNull String category, boolean background) {
        log(TAG + " updateNewsListData -> " + " category -> " + category);
        if (timeLastNetworkRequest(category) > MINIMUM_WAIT_NETWORK_REQUEST) {
            if (background) { view.showBackgroundWorkProgress(true); }
            else { view.showUpdateProgress(true); }
            disposable.add(Api.NewsApi().getNewsListFromNetwork1(category, 0)
                    .subscribeOn(Schedulers.io())
                    .map(RealmMapping::getMappingUpdateNewsList)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(list -> {
                        if (background) { view.showBackgroundWorkProgress(false); }
                        else { view.showUpdateProgress(false); }
                        List<NewsModel> cacheDb = mRealm.where(NewsModel.class).equalTo("category", category).findAllAsync();
                        List<NewsModel> cache = checkList(list, cacheDb);
                        int size = cache.size();
                        if (size > 0) {
                            view.showPopUp(size + " новых новостей");
                            view.updateDataList(cache);
                            if (size == list.size()) {
                                // Значит кэш настолько стара, что там новостей больше чем одна страница
                                loadMoreNewItems(category, pageNumber, cache.get(0).link);
                            }
                            showTopComments(list, category);
                            mRealm.executeTransactionAsync(realm -> realm.insertOrUpdate(cache));
                            timeWaitUpdate.put(category, System.currentTimeMillis());
                            if (size > 30) {
                                removeOldNews(category);
                            }
                        } else {
                            view.showPopUp("Нет новых новостей");
                        }
                    }, throwable -> {
                        if (background) { view.showBackgroundWorkProgress(false); }
                        else { view.showUpdateProgress(false); }
                        view.showToast("Не могу обновить список.");
                    }));
        }
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
     */
    @Override
    public void loadMore(@NonNull String category, int pageNumber) {
        disposable.add(Api.NewsApi().getNewsListFromNetwork1(category, pageNumber)
                .subscribeOn(Schedulers.io())
                .map(RealmMapping::getMappingNewsList)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> view.showLoadMore(list), throwable -> view.showErrorLoadMore()));
    }

    private void loadMoreNewItems(@NonNull String category, int pageNumber, String lastUrl) {
        log(TAG + " LoadMoreNewItems");

        view.updateLoadMore(null);
        List<NewsModel> cache = new ArrayList<>();
        disposable.add(Api.NewsApi().getNewsListFromNetwork1(category, pageNumber)
                .subscribeOn(Schedulers.io())
                .map(RealmMapping::getMappingUpdateNewsList)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    Stream.of(list).filterNot(value -> value.link.equals(lastUrl)).forEach(cache::add);
                    view.updateLoadMore(cache);
                }, throwable -> view.showToast("Не могу загрузить больше новостей.")));
    }

    private long timeLastNetworkRequest(@NonNull String category) {
        Long lastRequest = timeWaitUpdate.get(category);
        if (lastRequest != null) {
            return TimeUnit.SECONDS.convert(System.currentTimeMillis() - lastRequest, TimeUnit.MILLISECONDS);
        } else {
            return Long.MAX_VALUE;
        }
    }

    private List<NewsModel> checkList(List<NewsModel> list, List<NewsModel> cache2) {
        List<NewsModel> cache = new ArrayList<>();
        Stream.of(list).filterNot(newModel -> Stream.of(cache2)
                .anyMatch(oldModel -> newModel.link.equals(oldModel.link)))
                .forEach(cache::add);
        return cache;
    }

    private void removeOldNews(String category) {
        // rewrite
        List<NewsModel> cacheDb = mRealm.where(NewsModel.class).equalTo(NewsModel.CATEGORY, category).findAllAsync();
        List<NewsModel> cache = new ArrayList<>();
        int i = 0;
        while (i < 30) {
            cache.add(cacheDb.get(i));
            i++;
        }
        mRealm.executeTransactionAsync(realm -> {
            realm.delete(NewsModel.class);
            realm.insert(cache);
        });
    }

    private void showTopComments(List<NewsModel> list, String category) {
        // rewrite
        if (Preferences.News.getShowTopCommentsNew()) {
            List<TopNewsModel> cache = mRealm.where(TopNewsModel.class).findAll();
            if (!cache.isEmpty()) {
                view.showTopCommentsNews(cache);
                if (Preferences.News.getListTimeUpdateTop(category) + MINIMUM_UPDATE_WAIT > System.currentTimeMillis()) {
                    List<TopNewsModel> cache1 = new ArrayList<>();
                    Stream.of(list)
                            .filterNot(nValue -> Stream.of(cache)
                                    .anyMatch(oValue -> nValue.link.equals(oValue.link)))
                            .map(RealmMapping::mappingNewsToTop)
                            .filter(value -> value.commentsCount != null)
                            .forEach(cache1::add);
                    TopNewsModel model = RealmMapping.getTopModel(cache1);
                    view.updateTopCommentsNews(model);
                    mRealm.executeTransaction(realm -> realm.insertOrUpdate(model));
                    Preferences.News.setTimeUpdateTopList(category, System.currentTimeMillis());
                }
            } else {
                List<TopNewsModel> cache1 = new ArrayList<>();
                Stream.of(list).map(RealmMapping::mappingNewsToTop).forEach(cache1::add);
                TopNewsModel model = RealmMapping.getTopModel(cache1);
                view.showTopCommentsNews(null);
                view.updateTopCommentsNews(model);
                mRealm.executeTransaction(realm -> realm.insertOrUpdate(model));
                Preferences.News.setTimeUpdateTopList(category, System.currentTimeMillis());
            }
        }
    }
}
