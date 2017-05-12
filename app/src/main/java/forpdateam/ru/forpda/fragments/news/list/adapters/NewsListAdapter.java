package forpdateam.ru.forpda.fragments.news.list.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.fragments.news.callbacks.NewsListClickListener;
import forpdateam.ru.forpda.fragments.news.list.adapters.holders.CompatNewsListHolder;
import forpdateam.ru.forpda.fragments.news.list.adapters.holders.FullNewsListHolder;
import forpdateam.ru.forpda.models.news.NewsModel;
import forpdateam.ru.forpda.pref.Preferences;
import forpdateam.ru.forpda.widgets.recycleradapter.RecyclerBindableAdapter;

/**
 * Created by isanechek on 5/3/17.
 */

public class NewsListAdapter extends RecyclerBindableAdapter<NewsModel, RecyclerView.ViewHolder> {

    private NewsListClickListener mListener;
    private boolean itemCompat = Preferences.News.getCompatItem();

    @Override
    protected int layoutId(int type) {
        if (itemCompat) {
            return R.layout.news_list_item_compat;
        }
        return R.layout.news_list_item_full;
    }

    @Override
    protected void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position, int type) {
        if (itemCompat) {
            CompatNewsListHolder compatHolder = (CompatNewsListHolder) viewHolder;
            compatHolder.bindView(getItem(position), position, mListener);
        } else {
            FullNewsListHolder fullHolder = (FullNewsListHolder) viewHolder;
            fullHolder.bindView(getItem(position), position, mListener);
        }

    }

    @Override
    protected RecyclerView.ViewHolder viewHolder(View view, int type) {
        if (itemCompat) {
            return new CompatNewsListHolder(view);
        }
        return new FullNewsListHolder(view);
    }


    public void setActionListener(NewsListClickListener listener) {
        this.mListener = listener;
    }
}
