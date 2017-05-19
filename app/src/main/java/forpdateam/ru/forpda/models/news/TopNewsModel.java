package forpdateam.ru.forpda.models.news;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by isanechek on 5/7/17.
 */

public class TopNewsModel extends RealmObject {

    public static final long TIME_TO_DIE = 21600000; //6 hours

    @PrimaryKey
    public String link;
    public String imgUrl;
    public String title;
    public String commentsCount;
    public String category;
    public long timeDia;
}
