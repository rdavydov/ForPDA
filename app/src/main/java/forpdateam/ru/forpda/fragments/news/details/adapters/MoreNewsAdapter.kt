package forpdateam.ru.forpda.fragments.news.details.adapters

import android.view.View
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.fragments.news.details.holders.MoreNewsHolder
import forpdateam.ru.forpda.models.news.MoreNewsModel
import forpdateam.ru.forpda.widgets.recycleradapter.RecyclerBindableAdapter

/**
 * Created by isanechek on 5/30/17.
 */
public class MoreNewsAdapter : RecyclerBindableAdapter<MoreNewsModel, MoreNewsHolder>() {

    private var listener: MoreNewsHolder.OnClickListener? = null

    override fun onBindItemViewHolder(viewHolder: MoreNewsHolder?, position: Int, type: Int) {
        viewHolder?.bindView(getItem(position), position, listener)
    }

    override fun viewHolder(view: View?, type: Int): MoreNewsHolder {
        return MoreNewsHolder(view)
    }

    override fun layoutId(type: Int): Int {
        return R.id.more_news_layout
    }

    fun setListener(listener: MoreNewsHolder.OnClickListener) {
        this.listener = listener
    }

}