package forpdateam.ru.forpda.models.news;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by isanechek on 5/7/17.
 */

public class TopNewsModel extends RealmObject {

    @PrimaryKey
    public String link;
    public String imgUrl;
    public String title;
    public String commentsCount;
}
