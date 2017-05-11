package forpdateam.ru.forpda.models.news;

import org.jsoup.nodes.Element;

import javax.annotation.PropertyKey;

import io.realm.RealmObject;

/**
 * Created by isanechek on 5/10/17.
 */

public class NCModel extends RealmObject {

    @PropertyKey
    public int postId;
    public String username;
    public String userProfUrl;
    public String text;
    public String postDate;
    public String reply;
}
