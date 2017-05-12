package forpdateam.ru.forpda.fragments.news.list.adapters.holders;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lid.lib.LabelImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.fragments.news.callbacks.NewsListClickListener;
import forpdateam.ru.forpda.models.news.NewsModel;

/**
 * Created by isanechek on 5/12/17.
 */

public class CompatNewsListHolder extends RecyclerView.ViewHolder {

    private LinearLayout root;
    private LabelImageView pic;
    private TextView title;
    private TextView date;
    private TextView author;
    private NewsListClickListener mListener;

    public CompatNewsListHolder(View view) {
        super(view);
        root = (LinearLayout) view.findViewById(R.id.compat_list_item_root);
        pic = (LabelImageView) view.findViewById(R.id.compat_list_item_pic);
        title = (TextView) view.findViewById(R.id.compat_list_item_title);
        date = (TextView) view.findViewById(R.id.compat_list_item_date);
        author = (TextView) view.findViewById(R.id.compat_list_item_author);
    }

    public void bindView(NewsModel item, int position, NewsListClickListener listener) {
        this.mListener = listener;

        root.setOnClickListener(v -> mListener.click(v, position));
        root.setOnLongClickListener(v -> {
            mListener.longClick(position);
            return true;
        });

        pic.setLabelVisual(item.newNews);
        pic.setLabelTextColor(Color.parseColor("#212121"));
        ImageSize size = new ImageSize(80, 80);
        ImageLoader.getInstance().displayImage("http:"+item.imgLink, pic, size);
        title.setText(item.title);
        date.setText(item.date);
        author.setText(item.author);
    }
}
