package forpdateam.ru.forpda.fragments.news.details;

import java.util.List;

import forpdateam.ru.forpda.models.news.MoreNewsModel;
import forpdateam.ru.forpda.models.news.NCModel;
import forpdateam.ru.forpda.models.news.TagModel;
import io.realm.RealmList;

/**
 * Created by isanechek on 5/4/17.
 */

public interface NDView {
    void showTopHeader(String commentsCount,
                       String date,
                       String author,
                       RealmList<TagModel> tagList);
    void showData(String html);
    void showMoreNews(List<MoreNewsModel> list);
    void showNextPrevPage(List<String> list);
    void showComments(List<NCModel> list);
}
