package forpdateam.ru.forpda.models.news;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by isanechek on 5/10/17.
 */

public class NCModel extends RealmObject {

    @PrimaryKey
    public String postId;
    public String username;
    public String userProfUrl;
    public String text;
    public String postDate;
    public String reply;
}
