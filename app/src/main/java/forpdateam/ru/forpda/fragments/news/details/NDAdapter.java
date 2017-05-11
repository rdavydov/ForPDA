package forpdateam.ru.forpda.fragments.news.details;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.fragments.news.details.holders.NDCViewHolder;
import forpdateam.ru.forpda.models.news.NewsCommentModel;
import forpdateam.ru.forpda.widgets.recycleradapter.RecyclerBindableAdapter;

/**
 * Created by isanechek on 5/4/17.
 */

public class NDAdapter extends RecyclerBindableAdapter<NewsCommentModel, RecyclerView.ViewHolder> {

    private static final int FULL_ITEM = 0;
    private static final int COMPAT_ITEM = 1;

    @Override
    protected int layoutId(int type) {
        return R.layout.news_details_comments_item;
    }

    @Override
    protected void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position, int type) {
        NDCViewHolder commentHolder = (NDCViewHolder) holder;
        commentHolder.bindView(getItem(position), position);
    }

    @Override
    protected RecyclerView.ViewHolder viewHolder(View view, int type) {
//        if (type == IMG_BLOCK) {
//            return new DNIViewHolder(view);
//        } else if (type == WEB_BLOCK) {
//            return new DNWViewHolder(view);
//        }
        return new NDCViewHolder(view);
    }

    @Override
    protected int getItemType(int position) {

        return super.getItemType(position);
    }
}
