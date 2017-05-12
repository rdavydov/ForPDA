package forpdateam.ru.forpda.fragments.news.list.adapters.holders;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lid.lib.LabelImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.fragments.news.callbacks.NewsListClickListener;
import forpdateam.ru.forpda.models.news.NewsModel;
import forpdateam.ru.forpda.views.FixCardView;

/**
 * Created by isanechek on 5/3/17.
 */

public class FullNewsListHolder extends RecyclerView.ViewHolder {

    private NewsListClickListener mListener;
    private FixCardView card;
    private LabelImageView pic;
    private TextView title;
    private TextView date;
    private TextView description;
    private TextView author;
    private TextView commentsCount;
    private LinearLayout mTagGroup;

    public FullNewsListHolder(View view) {
        super(view);
        card = (FixCardView) view.findViewById(R.id.news_list_item_root_card);
        pic = (LabelImageView) view.findViewById(R.id.news_list_item_pic);
        title = (TextView) view.findViewById(R.id.news_list_item_title_tv);
        date = (TextView) view.findViewById(R.id.news_list_item_date_tv);
        description = (TextView) view.findViewById(R.id.news_list_item_description_tv);
        author = (TextView) view.findViewById(R.id.news_list_item_author_tv);
        commentsCount = (TextView) view.findViewById(R.id.news_list_item_comments_tv);
        mTagGroup = (LinearLayout) view.findViewById(R.id.news_list_item_tag_group);
    }

    public void bindView(NewsModel item, int position, NewsListClickListener listener) {
        this.mListener = listener;

        card.setOnClickListener(v -> mListener.click(v, position));
        card.setOnLongClickListener(v -> {
            mListener.longClick(position);
            return true;
        });

        pic.setLabelVisual(item.newNews);
        pic.setLabelTextColor(Color.parseColor("#212121"));
        ImageSize size = new ImageSize(100, 100);
        ImageLoader.getInstance().displayImage("http:"+item.imgLink, pic, size);
        title.setText(item.title);
        description.setText(item.description);
        date.setText(item.date);
        author.setText(item.author);
        commentsCount.setText(item.commentsCount);

        /*For Debug*/
        List<String> tags = new ArrayList<>();
        tags.add("huinya");
        tags.add("zopa");
        tags.add("v hui");
        /*==========================*/
        for (String tag: tags) {
            TextView tagView = new TextView(itemView.getContext());
            tagView.setText(tag);
            tagView.setTextSize(10f);
            tagView.setTextColor(ContextCompat.getColor(tagView.getContext(), R.color.text_drawer_item_color));
            tagView.setPadding(4, 0, 0, 0);
            mTagGroup.addView(tagView);
        }
    }
}
