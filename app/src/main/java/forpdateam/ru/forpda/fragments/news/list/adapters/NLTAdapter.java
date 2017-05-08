package forpdateam.ru.forpda.fragments.news.list.adapters;

import android.view.View;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.fragments.news.list.adapters.holders.NLTViewHolder;
import forpdateam.ru.forpda.models.news.NewsModel;
import forpdateam.ru.forpda.models.news.TopNewsModel;
import forpdateam.ru.forpda.widgets.recycleradapter.RecyclerBindableAdapter;

/**
 * Created by isanechek on 5/5/17.
 */

public class NLTAdapter extends RecyclerBindableAdapter<TopNewsModel, NLTViewHolder> {

    private NLTViewHolder.ActionListener mListener;

    @Override
    protected int layoutId(int type) {
        return R.layout.news_list_top_item;
    }

    @Override
    protected NLTViewHolder viewHolder(View view, int type) {
        return new NLTViewHolder(view);
    }

    @Override
    protected void onBindItemViewHolder(NLTViewHolder viewHolder, int position, int type) {
        viewHolder.bindView(getItem(position), position, mListener);
    }

    public void setActionListener(NLTViewHolder.ActionListener listener) {
        this.mListener = listener;
    }
}
