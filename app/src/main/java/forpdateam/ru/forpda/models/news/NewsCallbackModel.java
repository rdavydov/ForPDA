package forpdateam.ru.forpda.models.news;

import java.util.List;

/**
 * Created by isanechek on 12.01.17.
 */

public class NewsCallbackModel {

    public List<NewsModel> cache;
    public boolean fromNetwork;
    public boolean showMore;
    public NewsExceptionModel model;

    public NewsCallbackModel(List<NewsModel> cache, boolean fromNetwork, boolean showMore) {
        this.cache = cache;
        this.fromNetwork = fromNetwork;
        this.showMore = showMore;
    }

    public NewsCallbackModel(List<NewsModel> cache) {
        this.cache = cache;
    }

    public NewsCallbackModel(NewsExceptionModel model) {
        this.model = model;
    }

    public NewsCallbackModel() {}
}
