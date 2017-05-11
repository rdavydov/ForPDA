package forpdateam.ru.forpda.models.news;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by isanechek on 5/8/17.
 */

public class NewsDetailsModel extends RealmObject {

    @PrimaryKey
    public String link;
    public String imgLink;
    public String title;
    public String commentsCount;
    public String date;
    public String author;
    public RealmList<TagModel> tagList;
    // Original page source
    public String html;
    // Content source
    public String content;
    // More news source
    public String moreNews;
    // Comments source
    public String commentsHtml;
    // true если юзер оставил хоть один комментарий
    public boolean commenting;
    // Id полседнего комментария
    public int myCommentId;
    // Время жизни обьекта
    public long timeIsDie;
}
