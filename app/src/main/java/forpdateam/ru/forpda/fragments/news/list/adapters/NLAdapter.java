package forpdateam.ru.forpda.fragments.news.list.adapters;

import android.view.View;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.fragments.news.list.adapters.holders.NewsViewHolder;
import forpdateam.ru.forpda.models.news.NewsModel;
import forpdateam.ru.forpda.widgets.recycleradapter.RecyclerBindableAdapter;

/**
 * Created by isanechek on 5/3/17.
 */

public class NLAdapter extends RecyclerBindableAdapter<NewsModel, NewsViewHolder> {

    private NewsViewHolder.ActionListener mListener;

    @Override
    protected int layoutId(int type) {
        return R.layout.news_list_item_full;
    }

    @Override
    protected NewsViewHolder viewHolder(View view, int type) {
        return new NewsViewHolder(view);
    }

    @Override
    protected void onBindItemViewHolder(NewsViewHolder viewHolder, int position, int type) {
        viewHolder.bindView(getItem(position), position, mListener);
    }

    public void setActionListener(NewsViewHolder.ActionListener listener) {
        this.mListener = listener;
    }
}
