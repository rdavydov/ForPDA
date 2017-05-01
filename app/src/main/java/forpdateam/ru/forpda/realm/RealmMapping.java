package forpdateam.ru.forpda.realm;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.api.news.models.NewsNetworkModel;
import forpdateam.ru.forpda.fragments.news.models.NewsModel;

/**
 * Created by isanechek on 28.09.16.
 */

public class RealmMapping {

    public static NewsModel mappingNews(NewsNetworkModel item) {
        NewsModel newsModel = new NewsModel();
        newsModel.link = item.getLink();
        newsModel.imgLink = item.getImageUrl();
        newsModel.author = item.getAuthor();
        newsModel.commentsCount = item.getCommentsCount();
        newsModel.date = item.getDate();
        newsModel.description = item.getDescription();
        newsModel.title = item.getTitle();
        newsModel.category = item.getCategory();
        return newsModel;
    }

    public static List<NewsModel> getMappingNewsList(ArrayList<NewsNetworkModel> networkModels) {
        ArrayList<NewsModel> cache = new ArrayList<>();
        Stream.of(networkModels).map(RealmMapping::mappingNews).forEach(cache::add);
        return cache;
    }
}
