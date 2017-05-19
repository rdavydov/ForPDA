package forpdateam.ru.forpda.fragments.news.list.adapters.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import forpdateam.ru.forpda.R;

/**
 * Created by isanechek on 5/13/17.
 */

public class LoadMoreNewsHolder extends RecyclerView.ViewHolder {
    private ProgressBar mProgressBar;
    public LoadMoreNewsHolder(View itemView) {
        super(itemView);
        mProgressBar = (ProgressBar) itemView.findViewById(R.id.news_load_more_progress);
    }
}
