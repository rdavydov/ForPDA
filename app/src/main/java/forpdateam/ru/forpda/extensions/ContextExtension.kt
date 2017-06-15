package forpdateam.ru.forpda.extensions

import android.content.Context
import android.support.v4.app.Fragment
import android.util.TypedValue

/**
 * Created by isanechek on 5/30/17.
 */
fun Fragment.dp(dpValue: Float): Float = context.dp(dpValue)

fun Context.dp(dpValue : Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, resources.displayMetrics)

fun Fragment.dp(dpValue: Int): Int = context.dp(dpValue)

fun Context.dp(dpValue : Int) : Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue.toFloat(), resources.displayMetrics).toInt()

