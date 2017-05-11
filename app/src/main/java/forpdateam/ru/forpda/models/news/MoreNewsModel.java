package forpdateam.ru.forpda.models.news;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by isanechek on 5/10/17.
 */

public class MoreNewsModel extends RealmObject {

    @PrimaryKey
    public String link;
    public String title;
    public String imgUrl;
}
