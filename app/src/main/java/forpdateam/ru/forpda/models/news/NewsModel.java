package forpdateam.ru.forpda.models.news;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by isanechek on 28.09.16.
 */

public class NewsModel extends RealmObject {
    public static final String CATEGORY = "category";
    public static final long TIME_TO_DIE = 259200000; // 3 day's

    @PrimaryKey
    public String link;
    public String category;
    public String imgLink;
    public String title;
    public String commentsCount;
    public String date;
    public String author;
    public String description;
    public RealmList<TagModel> tagList;
    // Отмечает как true когда открыл новость, item в листе меняет цвет
    public boolean read;
    // true когда update возращает новые item's
    public boolean newNews;
    // Скрывает из основного потока
    public boolean hide;
    // Тут сохраняется исходник страницы
    public String html;
    // Проверяет пришло ли время дохнуть. То есть прошло ли три дня с момента последнего обращения
    public long timeIsDie;

}
