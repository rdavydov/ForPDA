package forpdateam.ru.forpda.fragments.news.details.holders

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.extensions.dp
import forpdateam.ru.forpda.extensions.loadImageFromNetwork
import forpdateam.ru.forpda.extensions.lparams
import forpdateam.ru.forpda.models.news.MoreNewsModel

/**
 * Created by isanechek on 5/30/17.
 */
class MoreNewsHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    private var listener: MoreNewsHolder.OnClickListener? = null

    fun bindView(model: MoreNewsModel, position: Int, listener: MoreNewsHolder.OnClickListener?) {
        this.listener = listener
        val ctx = itemView.context
        val root = CardView(ctx)
        root.id = R.id.more_news_layout
        root.lparams(ctx.dp(100), ctx.dp(148))
        root.setOnClickListener { listener?.click(position, it) }
        val container = LinearLayout(ctx)
        container.lparams(MATCH_PARENT, MATCH_PARENT)
        container.orientation = LinearLayout.VERTICAL
        val image = ImageView(ctx)
        image.lparams(ctx.dp(100), ctx.dp(100))
        image.loadImageFromNetwork(model.imgUrl)
        container.addView(image)
        val title = TextView(ctx)
        title.text = model.title
        container.addView(title)
        root.addView(container)
    }

    interface OnClickListener {
        fun click(position: Int, view: View)
        fun longClick(position: Int)
    }
}