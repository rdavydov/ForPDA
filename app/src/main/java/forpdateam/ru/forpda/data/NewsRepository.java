package forpdateam.ru.forpda.data;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Predicate;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.data.network.NewsLoader;
import forpdateam.ru.forpda.models.news.NewsCallbackModel;
import forpdateam.ru.forpda.models.news.NewsModel;
import forpdateam.ru.forpda.realm.RealmMapping;
import forpdateam.ru.forpda.utils.rx.RxSchedulers;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.realm.Realm;

import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by isanechek on 13.01.17.
 */

public class NewsRepository implements Closeable, IRepository {

    private static final long MINIMUM_NETWORK_WAIT_SEC = 120;
    private static final String TAG = "NewsRepository";
    private static NewsRepository INSTANCE;

    private Realm mRealm;
    private final NewsLoader mNewsLoader;
    private Map<String, Long> lastNetworkRequest = new HashMap<>();
    private CompositeDisposable disposable;

    public static void createInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NewsRepository();
        }
    }

    public static NewsRepository getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("No >>News<< Repository Instance Available");
        }
        return INSTANCE;
    }

    public void removeInstance() {
        INSTANCE = null;
        if (disposable != null) {
            disposable.clear();
        }

        if (mRealm != null) {
            mRealm.close();
        }
    }

    @UiThread
    private NewsRepository() {
        log("NewsRepository");
//        mRealm = Realm.getInstance(App.getInstance().getNewsRealmConfig());
        mNewsLoader = new NewsLoader();
        disposable = new CompositeDisposable();
    }


    /**
     *
     * @param category - категория новостей
     * @return - объект со списком новостей либо из базы, либо из сети и сохраняет в базу.
     * Плюс первый boolean - откуда пришли данные. True - сеть, false - база.
     * Второй не используется
     *
     * ps. Надо шоб показать прогресс и для запуска обновления.
     */
    @Override
    public Single<NewsCallbackModel> getNewsList(@NonNull String category) {
        log("get list category --> " + category);
        List<NewsModel> cache = mRealm.where(NewsModel.class).equalTo("category", category).findAll();
        log("get list size " + cache.size());
        if (cache.size() > 0) {
            getNewsAndSaveFromNetwork(category);
        }
        return Single.fromCallable(() -> new NewsCallbackModel(cache, false, false));
    }

    private void getNewsAndSaveFromNetwork(String category) {

        disposable.add(Api.NewsList().getNewsListFromNetwork1(category, 0)
                .subscribeOn(Schedulers.io())
                .map(RealmMapping::getMappingUpdateNewsList)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    log("From net size " + list.size());
                    mRealm.executeTransaction(realm -> realm.insertOrUpdate(list));
                    NewsModel model = getTopModel(list);
                    mRealm.executeTransaction(realm -> realm.insertOrUpdate(model));
                }));

    }

    @Override
    public Single<List<NewsModel>> getTopCommentsNews() {
        return Single.fromCallable(() -> mRealm.where(NewsModel.class).equalTo("topComment", true).findAll());
    }

    @Override
    public boolean updateRead(@NonNull String id) {
       final boolean status = false;
        mRealm.executeTransaction(realm -> {
            NewsModel model = realm.where(NewsModel.class).equalTo("link", id).findFirst();
            model.read = true;
            model.timeIsDie = System.currentTimeMillis();
        });
        boolean read = mRealm.where(NewsModel.class).equalTo("link", id).findFirst().read;
        return read;
    }

    /**
     *
     * @param category - категория новостей
     * @return - возращает список новых новостей в категории и обновляет базу.
     */
    @Override
    public Single<NewsCallbackModel> updateNewsListData(@NonNull String category) {
        return  Api.NewsList().getNewsListFromNetwork1(category, 0)
                .map(RealmMapping::getMappingNewsList)
                .flatMap(list -> {
                    List<NewsModel> cache = checkList(list, mRealm.where(NewsModel.class).equalTo("category", category).findAll());
                    mRealm.executeTransaction(realm -> realm.insertOrUpdate(cache));
                    return Single.fromCallable(() -> new NewsCallbackModel(cache));
                });
    }

    private List<NewsModel> checkList(List<NewsModel> list, List<NewsModel> cache2) {
        List<NewsModel> cache = new ArrayList<>();
        Stream.of(list).filterNot(newModel -> Stream.of(cache2)
                .anyMatch(oldModel -> newModel.link.equals(oldModel.link)))
                .forEach(cache::add);
        return cache;
    }


    /**
     * НУЖНО ДЛЯ ХИТРОВЫЕБНОЙ ПОГИНАЦИИ
     * @param category - категория новостей
     * @param pageNumber - номер страницы новостей
     * @param lastUrl - трудно объяснить, см ниже по коду.
     * @return - возращает объект со списком новостей и boolean полем.
     * Если true - значит сслыка не найдена в базе и есть чего еще грузить.
     * Если false - значит ссылка в базе найдена и нахер с пляжа.
     *
     * ps. Так же присутствует обновление базы до актуального списка.
     * pss. Если не понятно что происходит ниже, то не переживай - я тоже с трудом понимаю.)
     */
    @Override
    public Single<NewsCallbackModel> getLoadMoreNewsListData(@NonNull String category, int pageNumber, String lastUrl) {
        return Api.NewsList().getNewsListFromNetwork1(category, pageNumber)
                .map(RealmMapping::getMappingNewsList)
                .flatMap(new Function<List<NewsModel>, SingleSource<? extends NewsCallbackModel>>() {
                    @Override
                    public SingleSource<? extends NewsCallbackModel> apply(List<NewsModel> list) throws Exception {

                        // Логика чуть позже будет

                        return Single.fromCallable(() -> new NewsCallbackModel(list, false, false));
                    }
                });
    }


    /**
     * ЭТО ПРОСТО ПОГИНАЦИЯ
     * @param category - категория новостей
     * @param pageNumber - номер страницы новостей
     * @return - возращает список новостей cо страницы 2+.
     */
    @Override
    public Single<List<NewsModel>> loadMoreNewsItems(@NonNull String category, int pageNumber) {
        return  Api.NewsList()
                .getNewsListFromNetwork1(category, pageNumber)
                .map(RealmMapping::getMappingNewsList);
    }



    private NewsModel getTopModel(List<NewsModel> list) {
        return Stream.of(list).max((o1, o2) -> Integer.parseInt(o1.commentsCount) - Integer.parseInt(o2.commentsCount)).get();
    }

    @Override
    public void close() throws IOException {

    }
}
