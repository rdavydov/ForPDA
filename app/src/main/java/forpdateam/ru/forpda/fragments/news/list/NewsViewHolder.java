package forpdateam.ru.forpda.fragments.news.list;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lid.lib.LabelImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.fragments.news.models.NewsModel;
import me.gujun.android.taggroup.TagGroup;

/**
 * Created by isanechek on 5/3/17.
 */

public class NewsViewHolder extends RecyclerView.ViewHolder {

    private int position;

    LinearLayout card;
    LabelImageView pic;
    TextView title;
    TextView date;
    TextView description;
    TextView author;
    TextView commentsCount;
    TagGroup mTagGroup;

    public NewsViewHolder(View view) {
        super(view);
        card = (LinearLayout) view.findViewById(R.id.news_list_item_root_card);
        pic = (LabelImageView) view.findViewById(R.id.news_list_item_pic);
        title = (TextView) view.findViewById(R.id.news_list_item_title_tv);
        date = (TextView) view.findViewById(R.id.news_list_item_date_tv);
        description = (TextView) view.findViewById(R.id.news_list_item_description_tv);
        author = (TextView) view.findViewById(R.id.news_list_item_author_tv);
        commentsCount = (TextView) view.findViewById(R.id.news_list_item_comments_tv);
        mTagGroup = (TagGroup) view.findViewById(R.id.news_list_item_tag_group);
    }

    public void bindView(NewsModel item, int position) {
        title.setText(item.title);
        description.setText(item.description);
        date.setText(item.date);
        author.setText(item.author);
//        commentsCount.setText(item.commentsCount);
        pic.setLabelVisual(true);
        pic.setLabelTextColor(Color.parseColor("#212121"));
        List<String> tags = new ArrayList<>();
        tags.add("huinya");
        tags.add("zopa");
        tags.add("v hui");
//        Stream.of(newsModel.tagList)
//                .map(tagModel -> tagModel.title)
//                .forEach(tags::add);
        mTagGroup.setTags(tags);
        ImageLoader.getInstance().displayImage(item.imgLink, pic);
    }
}
