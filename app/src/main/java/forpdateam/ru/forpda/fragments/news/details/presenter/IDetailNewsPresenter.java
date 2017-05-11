package forpdateam.ru.forpda.fragments.news.details.presenter;

import android.support.annotation.NonNull;

import forpdateam.ru.forpda.fragments.news.details.NDView;

/**
 * Created by isanechek on 5/8/17.
 */

interface IDetailNewsPresenter {
    void bindView(NDView view);
    void unbindView();

    void loadModel(@NonNull String id);

    void loadContentData(@NonNull String url);
}
