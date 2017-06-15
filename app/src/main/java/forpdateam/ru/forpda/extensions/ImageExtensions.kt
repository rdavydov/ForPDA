package forpdateam.ru.forpda.extensions

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.views.FixCardView

/**
 * Created by isanechek on 5/21/17.
 */
fun ImageView.loadImageFromNetwork(url: String) {
    ImageLoader.getInstance().displayImage(url, this)
}

fun LinearLayout.addCustom(view: View) : View {
    val container = FixCardView(this.context)
    container.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    container.addView(view)
    addView(container)
    return this
}