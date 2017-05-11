package forpdateam.ru.forpda.fragments.news.details.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.models.news.NewsCommentModel;

/**
 * Created by isanechek on 5/4/17.
 */

public class NDCViewHolder extends RecyclerView.ViewHolder {

    TextView commentText;

    public NDCViewHolder(View itemView) {
        super(itemView);
        commentText = (TextView) itemView.findViewById(R.id.ndci_comment);
    }

    public void bindView(NewsCommentModel item, int position) {
        commentText.setText(item.comment);
    }
}
