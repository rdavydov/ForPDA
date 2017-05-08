package forpdateam.ru.forpda.fragments.news.list.adapters.holders;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lid.lib.LabelImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.models.news.NewsModel;
import forpdateam.ru.forpda.views.FixCardView;
import me.gujun.android.taggroup.TagGroup;

/**
 * Created by isanechek on 5/3/17.
 */

public class NewsViewHolder extends RecyclerView.ViewHolder {

    private int position;
    private ActionListener mListener;
    FixCardView card;
    LabelImageView pic;
    TextView title;
    TextView date;
    TextView description;
    TextView author;
    TextView commentsCount;
    LinearLayout mTagGroup;

    public NewsViewHolder(View view) {
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

    public void bindView(NewsModel item, int position, ActionListener listener) {
        this.mListener = listener;
        this.position = position;

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.click(v, position);
            }
        });

        title.setText(item.title);
        description.setText(item.description);
        date.setText(item.date);
        author.setText(item.author);
        commentsCount.setText(item.commentsCount);
        pic.setLabelVisual(true);
        pic.setLabelTextColor(Color.parseColor("#212121"));
        List<String> tags = new ArrayList<>();
        tags.add("huinya");
        tags.add("zopa");
        tags.add("v hui");
//        Stream.of(newsModel.tagList)
//                .map(tagModel -> tagModel.title)
//                .forEach(tags::add);
        for (String tag: tags) {
            TextView tagView = new TextView(itemView.getContext());
            tagView.setText(tag);
            tagView.setTextSize(8f);
            tagView.setPadding(4, 0, 4, 0);
            tagView.setBackgroundResource(R.drawable.circle_background1);
            mTagGroup.addView(tagView);
        }

        ImageLoader.getInstance().displayImage("http:"+item.imgLink, pic);
    }

    public interface ActionListener {
        void click(View view, int position);
        void longClick(int position);
    }
}
