package forpdateam.ru.forpda.fragments.news.models;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by isanechek on 28.09.16.
 */

public class NewsModel extends RealmObject {
    public static final String CATEGORY = "category";

    @PrimaryKey
    public String link;
    public String category;
    public String imgLink;
    public String title;
    public int commentsCount;
    public String date;
    public String author;
    public String description;
    public boolean read;
    public boolean newNews;
    public RealmList<TagModel> tagList;
}
