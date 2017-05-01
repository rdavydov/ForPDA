package forpdateam.ru.forpda.fragments.news;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Function;
import com.lid.lib.LabelImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.fragments.news.models.NewsModel;
import forpdateam.ru.forpda.fragments.news.models.TagModel;
import me.gujun.android.taggroup.TagGroup;

import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by isanechek on 28.09.16.
 */

public class NewsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "NewsListAdapter";

    public static final int ITEM = 0;
    public static final int LOADING = 1;
    private static final int VISIBLE_THRESHOLD = 5;
    private List<NewsModel> list;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnReloadDataListener mOnReloadDataListener;
    private boolean mIsLoadingFooterAdded = false;
    private ArrayList<NewsModel> mNewsList = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, View view);
    }

    public interface OnLoadMoreCallback {
        void onLoadMore();
    }

    public interface OnReloadDataListener {
        void onReloadDataClick();
    }

    public NewsListAdapter() {
        this.list = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case ITEM:
                viewHolder = createNewsViewHolder(parent);
                log(TAG + "onCreateViewHolder -> ITEM");
                break;
            case LOADING:
                viewHolder = createLoadMoreViewHolder(parent);
                log(TAG + "onCreateViewHolder -> PROGRESS");
                break;
            default:
                log(TAG + "NewsModel Adapter " +  "[ERR] type is not supported!!! type is %d: " + viewType);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ITEM:
                bindNewsViewHolder(holder, position);
                log(TAG + "onBindViewHolder -> ITEM");
                break;
            case LOADING:
                bindLoadMoreViewHolder(holder);
                log(TAG + "onBindViewHolder -> PROGRESS");
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == list.size()-1 && mIsLoadingFooterAdded) ? LOADING : ITEM;
    }

    private void add(NewsModel newsModel, boolean more) {
        list.add(newsModel);
//        int size = list.size();
//        notifyItemRangeChanged(more ? size+1 : size-1, 1);
//        notifyDataSetChanged();
    }

    public void addAll(List<NewsModel> results, boolean more) {
        list.addAll(results);
        int size = list.size();
        notifyItemRangeChanged(more ? size+1 : size-1, results.size());
        notifyDataSetChanged();
    }

    public void remove(NewsModel newsModel) {
        int position = list.indexOf(newsModel);
        if (position > -1) {
            list.remove(position);
            notifyItemRemoved(position);
        }
    }

    public NewsModel getNews(int position) {
        return list.get(position);
    }

    public void clear() {
        mIsLoadingFooterAdded = false;
        while (getItemCount() > 0) {
            remove(getNews(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void addMoreLoadingProgress(){
        mIsLoadingFooterAdded = true;
    }

    public void addMoreNewsLoadingItem(boolean show) {

    }

    public void removeMoreLoadingProgress() {
        mIsLoadingFooterAdded = false;

        int position = list.size() - 1;
        NewsModel newsModel = getNews(position);

        if (newsModel != null) {
            list.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.mOnItemLongClickListener = onItemLongClickListener;
    }

    public void setOnReloadDataListener(OnReloadDataListener onReloadDataListener) {
        this.mOnReloadDataListener = onReloadDataListener;
    }

    // --------------------------------------------------------------------------------------
    // *************************************NEWS ITEM****************************************
    // --------------------------------------------------------------------------------------

    private RecyclerView.ViewHolder createNewsViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_list_item2, parent, false);
        final NewsViewHolder holder = new NewsViewHolder(v);
        holder.card.setOnClickListener(v1 -> {
            int adapterPos = holder.getAdapterPosition();
            if(adapterPos != RecyclerView.NO_POSITION){
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(adapterPos, holder.itemView);
                }
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onItemLongClick(adapterPos, holder.itemView);
                }
            }
        });
        return holder;
    }

    private void bindNewsViewHolder(RecyclerView.ViewHolder viewHolder, int position){
        NewsViewHolder holder = (NewsViewHolder) viewHolder;
        final NewsModel newsModel = list.get(position);
        holder.title.setText(newsModel.title);
        holder.description.setText(newsModel.description);
        holder.date.setText(newsModel.date);
        holder.author.setText(newsModel.author);
        holder.commentsCount.setText(newsModel.commentsCount);
        holder.pic.setLabelVisual(true);
        holder.pic.setLabelTextColor(Color.parseColor("#212121"));
        List<String> tags = new ArrayList<>();
        tags.add("huinya");
        tags.add("zopa");
        tags.add("v hui");
//        Stream.of(newsModel.tagList)
//                .map(tagModel -> tagModel.title)
//                .forEach(tags::add);
        holder.mTagGroup.setTags(tags);
        ImageLoader.getInstance().displayImage(newsModel.imgLink, holder.pic);
    }

    private static class NewsViewHolder extends RecyclerView.ViewHolder {

        LinearLayout card;
//        ImageView pic;
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
    }

    // -------------------------------------------------------------------------------------- //
    // *************************************LOAD MORE**************************************** //
    // -------------------------------------------------------------------------------------- //

    private RecyclerView.ViewHolder createLoadMoreViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_item, parent, false);
        return new MoreLoadViewHolder(v);
    }

    private void bindLoadMoreViewHolder(RecyclerView.ViewHolder viewHolder) {
        MoreLoadViewHolder holder = (MoreLoadViewHolder) viewHolder;
        holder.progressBar.setVisibility(View.VISIBLE);
        holder.progressBar.setIndeterminate(true);
    }

    private static class MoreLoadViewHolder extends RecyclerView.ViewHolder {

        private Button reloadBtn;
        private ImageView error_face_iv;
        private TextView error_tv;
        private ProgressBar progressBar;

        public MoreLoadViewHolder(View item) {
            super(item);
            reloadBtn = (Button) item.findViewById(R.id.retry_btn);
            error_face_iv = (ImageView) item.findViewById(R.id.error_iv);
            error_tv = (TextView) item.findViewById(R.id.error_tv);
            progressBar = (ProgressBar) item.findViewById(R.id.progressBar);
        }
    }

    /*======================================TOP VIEW=============================================*/


    private RecyclerView.ViewHolder createTopViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_list_item_top, parent, false);
        return new TopViewHolder(v);
    }

    private void bindTopHolder(RecyclerView.ViewHolder viewHolder) {
        TopViewHolder holder = (TopViewHolder) viewHolder;
//        Stream.of(list).
//        holder.topTitle
    }

    private static class TopViewHolder extends RecyclerView.ViewHolder {

        ImageView topImage;
        TextView topTitle;

        public TopViewHolder(View itemView) {
            super(itemView);
            topImage = (ImageView) itemView.findViewById(R.id.news_list_top_item_iv);
            topTitle = (TextView) itemView.findViewById(R.id.news_list_top_item_title_tv);
        }
    }
}
