package forpdateam.ru.forpda.extensions

import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

/**
 * Created by isanechek on 5/30/17.
 */

inline fun <T: View> T.lparams(
        width: Int = android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
        height: Int = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
): T {
    val layoutParams = ViewGroup.LayoutParams(width, height)
    this@lparams.layoutParams = layoutParams
    return this
}

fun ViewGroup.extInflate(layoutId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)
}