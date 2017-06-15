package forpdateam.ru.forpda.fragments.news.details.holders

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.extensions.lparams
import forpdateam.ru.forpda.models.news.NewsCommentModel

/**
 * Created by isanechek on 6/4/17.
 */
class NewsCommentsHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    private var clickListener: NewsCommentsHolder.ClickListener? = null
    private var item: ConstraintLayout? = null
    private var username: TextView? = null
    private var time: TextView? = null
    private var date: TextView? = null
    private var text: TextView? = null
    private var likeBtn: ImageButton? = null
    private var replayBtn: Button? = null

    fun bindView(model: NewsCommentModel, position: Int, listener: NewsCommentsHolder.ClickListener?) {
        this.clickListener = listener

        text?.text = model.comment

        item?.setOnClickListener { clickListener?.click(it, position) }

    }

    interface ClickListener {
        fun click(view: View, position: Int)
        fun longClick(position: Int)
    }

    init {
        item = itemView?.findViewById(R.id.comment_item) as ConstraintLayout
        username = itemView.findViewById(R.id.comment_username) as TextView
        time = itemView.findViewById(R.id.comment_time) as TextView
        date = itemView.findViewById(R.id.comment_date) as TextView
        text = itemView.findViewById(R.id.comment_text) as TextView
        likeBtn = itemView.findViewById(R.id.comment_like_button) as ImageButton
        replayBtn = itemView.findViewById(R.id.comment_replay_button) as Button
    }
}