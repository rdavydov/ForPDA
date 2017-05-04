package forpdateam.ru.forpda.fragments.news.list;

import android.view.View;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.fragments.news.models.NewsModel;
import forpdateam.ru.forpda.widgets.recycleradapter.RecyclerBindableAdapter;

/**
 * Created by isanechek on 5/3/17.
 */

public class NewsListAdapter extends RecyclerBindableAdapter<NewsModel, NewsViewHolder> {

    @Override
    protected int layoutId(int type) {
        return R.layout.news_list_item2;
    }

    @Override
    protected NewsViewHolder viewHolder(View view, int type) {
        return new NewsViewHolder(view);
    }

    @Override
    protected void onBindItemViewHolder(NewsViewHolder viewHolder, int position, int type) {
        viewHolder.bindView(getItem(position), position);
    }
}
