package forpdateam.ru.forpda.fragments.news.list.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.fragments.news.callbacks.NewsListClickListener;
import forpdateam.ru.forpda.fragments.news.list.adapters.holders.CompatNewsListHolder;
import forpdateam.ru.forpda.fragments.news.list.adapters.holders.FullNewsListHolder;
import forpdateam.ru.forpda.models.news.NewsModel;
import forpdateam.ru.forpda.widgets.recycleradapter.RecyclerBindableAdapter;

import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by isanechek on 5/3/17.
 */

public class NewsListAdapter extends RecyclerBindableAdapter<NewsModel, RecyclerView.ViewHolder> {

    private static final int VIEW_ITEM_COMPAT = 0;
    private static final int VIEW_ITEM_FULL = 1;
    private NewsListClickListener mListener;

    @Override
    protected int layoutId(int type) {
        if (type == VIEW_ITEM_COMPAT) {
            return R.layout.news_list_item_compat;
        }
        return R.layout.news_list_item_full;
    }

    @Override
    protected void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position, int type) {
        if (type == VIEW_ITEM_COMPAT) {
            CompatNewsListHolder compatHolder = (CompatNewsListHolder) viewHolder;
            compatHolder.bindView(getItem(position), position, mListener);
        } else if (type == VIEW_ITEM_FULL){
            FullNewsListHolder fullHolder = (FullNewsListHolder) viewHolder;
            fullHolder.bindView(getItem(position), position, mListener);
        } else {
            log("Эррорчик...  Шо за гавно мне тут суешь, ублюдок? Ыы");
        }

    }

    @Override
    protected RecyclerView.ViewHolder viewHolder(View view, int type) {
        if (type == VIEW_ITEM_COMPAT) {
            return new CompatNewsListHolder(view);
        }
        return new FullNewsListHolder(view);
    }

    @Override
    protected int getItemType(int position) {
        // Need rewrite view xml
//        if (Preferences.News.getCompatItem()) {
//            return VIEW_ITEM_COMPAT;
//        }

        return VIEW_ITEM_FULL;
    }

    public void setActionListener(NewsListClickListener listener) {
        this.mListener = listener;
    }
}
