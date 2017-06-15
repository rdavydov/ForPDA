package forpdateam.ru.forpda.fragments.news.details

import android.text.Spanned
import forpdateam.ru.forpda.models.news.MoreNewsModel
import forpdateam.ru.forpda.models.news.NCModel

/**
 * Created by isanechek on 6/13/17.
 */
interface INewsDetailsView {
    fun showProgress(show: Boolean)
    fun showWebViewContent(html: String?)
    fun showMoreNews(list: List<MoreNewsModel>)
    fun showComments(comments: List<NCModel>)
    fun showError()
}