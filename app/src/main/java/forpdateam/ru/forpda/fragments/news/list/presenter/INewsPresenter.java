package forpdateam.ru.forpda.fragments.news.list.presenter;

import android.support.annotation.NonNull;

import forpdateam.ru.forpda.fragments.news.list.INewsView;

/**
 * Created by isanechek on 12.01.17.
 */

public interface INewsPresenter {

    void bindView(INewsView iNewsView);
    void unbindView();

    void loadData(String category);
    void updateData(@NonNull String category, boolean background);
    void reInstance(@NonNull String category, int size);
    void loadMore(@NonNull String category, int pageNumber);
}
