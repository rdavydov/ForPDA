package forpdateam.ru.forpda.fragments.news.list.adapters.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.models.news.NewsModel;
import forpdateam.ru.forpda.models.news.TopNewsModel;

/**
 * Created by isanechek on 5/5/17.
 */

public class NLTViewHolder extends RecyclerView.ViewHolder {
    private ActionListener mListener;
    private LinearLayout root;
    private ImageView pic;
    private TextView title;
    private TextView count;

    public NLTViewHolder(View view) {
        super(view);
        root = (LinearLayout) view.findViewById(R.id.news_list_top_item_root);
        pic = (ImageView) view.findViewById(R.id.news_list_top_pic);
        title = (TextView) view.findViewById(R.id.news_list_top_title);
        count = (TextView) view.findViewById(R.id.news_list_top_comments_count);
    }

    public void bindView(TopNewsModel item, int position, ActionListener listener) {
        this.mListener = listener;
        ImageLoader.getInstance().displayImage(item.imgUrl, pic);
        title.setText(item.title);
//        count.setText(item.commentsCount);
//        root.setOnClickListener(v -> listener.click(v, position));
//        root.setOnLongClickListener(v -> {
//            listener.longClick(position);
//            return true;
//        });
    }

    public interface ActionListener {
        void click(View view, int position);
        void longClick(int position);
    }
}
