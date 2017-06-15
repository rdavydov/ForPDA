package forpdateam.ru.forpda.fragments.news.details.adapters

import android.view.View
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.fragments.news.details.holders.NewsCommentsHolder
import forpdateam.ru.forpda.models.news.NewsCommentModel
import forpdateam.ru.forpda.widgets.recycleradapter.RecyclerBindableAdapter

/**
 * Created by isanechek on 6/4/17.
 */
class NewsDetailsAdapter : RecyclerBindableAdapter<NewsCommentModel, NewsCommentsHolder>() {

    private var click: NewsCommentsHolder.ClickListener? = null

    override fun onBindItemViewHolder(viewHolder: NewsCommentsHolder?, position: Int, type: Int) {
        viewHolder?.bindView(getItem(position), position, click)
    }

    override fun viewHolder(view: View?, type: Int): NewsCommentsHolder {
        return NewsCommentsHolder(view)
    }

    override fun layoutId(type: Int): Int {
        return R.id.comment_item_layout
    }

    fun setClickListener(listener: NewsCommentsHolder.ClickListener) {
        this.click = listener
    }

}