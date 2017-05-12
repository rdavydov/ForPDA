package forpdateam.ru.forpda.realm;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Predicate;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.models.news.NewsNetworkModel;
import forpdateam.ru.forpda.models.news.NewsModel;
import forpdateam.ru.forpda.models.news.TopNewsModel;

/**
 * Created by isanechek on 28.09.16.
 */

public class RealmMapping {

    private static NewsModel mappingNews(NewsNetworkModel item) {
        NewsModel newsModel = new NewsModel();
        newsModel.link = item.link;
        newsModel.imgLink = item.imageUrl;
        newsModel.author = item.author;
        newsModel.commentsCount = item.commentsCount;
        newsModel.date = item.date;
        newsModel.description = item.description;
        newsModel.title = item.title;
        newsModel.category = item.category;
        return newsModel;
    }

    private static NewsModel mappingUpdateNews(NewsNetworkModel item) {
        NewsModel newsModel = new NewsModel();
        newsModel.link = item.link;
        newsModel.imgLink = item.imageUrl;
        newsModel.author = item.author;
        newsModel.commentsCount = item.commentsCount;
        newsModel.date = item.date;
        newsModel.description = item.description;
        newsModel.title = item.title;
        newsModel.category = item.category;
        newsModel.newNews = true;
        return newsModel;
    }

    public static List<NewsModel> getMappingNewsList(ArrayList<NewsNetworkModel> networkModels) {
        ArrayList<NewsModel> cache = new ArrayList<>();
        Stream.of(networkModels).map(RealmMapping::mappingNews).forEach(cache::add);
        return cache;
    }

    public static List<NewsModel> getMappingUpdateNewsList(ArrayList<NewsNetworkModel> networkModels) {
        ArrayList<NewsModel> cache = new ArrayList<>();
        Stream.of(networkModels).map(RealmMapping::mappingUpdateNews).forEach(cache::add);
        return cache;
    }

    public static TopNewsModel getTopModel(List<TopNewsModel> list) {
        return Stream.of(list)
                .filter(value -> value.commentsCount != null)
                .max((o1, o2) -> Integer.parseInt(o1.commentsCount) - Integer.parseInt(o2.commentsCount))
                .get();
    }

    public static TopNewsModel mappingNewsToTop(NewsModel news) {
        TopNewsModel model = new TopNewsModel();
        model.link = news.link;
        model.imgUrl = news.imgLink;
        model.title = news.title;
        model.commentsCount = news.commentsCount;
        return model;
    }
}
