package forpdateam.ru.forpda.fragments.news.list;

import android.support.annotation.NonNull;

import java.util.List;

import forpdateam.ru.forpda.models.news.NewsCallbackModel;
import forpdateam.ru.forpda.models.news.NewsModel;
import forpdateam.ru.forpda.models.news.TopNewsModel;

/**
 * Created by isanechek on 12.01.17.
 */

public interface INewsView {
    void showData(List<NewsModel> list);
    void showTopCommentsNews(List<TopNewsModel> list);
    void updateTopCommentsList(TopNewsModel model);
    void updateDataList(List<NewsModel> list);
    void showLoadMore(List<NewsModel> list);
    void showUpdateProgress(boolean show);
    void showBackgroundWorkProgress(boolean show);
    void showErrorView(Throwable throwable, @NonNull String codeError);
    void showPopUp(@NonNull String viewCode);
}
