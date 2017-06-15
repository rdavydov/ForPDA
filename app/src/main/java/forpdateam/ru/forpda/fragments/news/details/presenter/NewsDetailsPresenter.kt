package forpdateam.ru.forpda.fragments.news.details.presenter

import forpdateam.ru.forpda.api.Api
import forpdateam.ru.forpda.extensions.async
import forpdateam.ru.forpda.fragments.news.details.INewsDetailsView
import forpdateam.ru.forpda.models.news.MoreNewsModel
import forpdateam.ru.forpda.models.news.NCModel
import forpdateam.ru.forpda.pref.Preferences
import forpdateam.ru.forpda.utils.Logger.log
import forpdateam.ru.forpda.utils.Logger.setTag
import forpdateam.ru.forpda.utils.html

/**
 * Created by isanechek on 5/28/17.
 */
class NewsDetailsPresenter : INewsDetailsPresenter {

    private var view: INewsDetailsView? = null
//    private var flavoredHtml: FlavoredHtml? = null

    override fun bind(view: INewsDetailsView) {
        this.view = view
//        flavoredHtml = FlavoredHtml.Builder(App.getContext())
//                .newLine("p", "h1", "h2", "h3", "h4", "h5", "h6", "li")
//                .textAppearance(R.style.headline, "h1")
//                .textAppearance(R.style.title, "h2")
//                .textAppearance(R.style.subhead, "h3")
//                .textAppearance(R.style.body, "p", "li")
//                .style(Typeface.BOLD, "b", "strong")
//                .style(Typeface.ITALIC, "i", "em")
//                .bullet(15, "li")
//                .build();
    }

    override fun unbind() {
        async.cancelAll()
    }

    fun loadPage(url: String) = async {
        log("loadPage $url")

        view?.showProgress(true)
        val result = await { Api.NewsApi().getDetailsNews(Api.getWebClient().get(url)) }
        val sourceContent = result.content
        if (sourceContent.isNotEmpty()) {
            view?.showProgress(false)
            // показываем контент в вебвью
            view?.showWebViewContent(
                    html {
                        head { +"" }
                        body { +sourceContent }
                    }.toString()
            )

//            // блок новостей по теме
//            val moreNews = await { Api.NewsApi().getDetailsMoreNewsJ(result.moreNews) }
//            val cache = ArrayList<MoreNewsModel>()
//            moreNews.forEach {
//                val model = MoreNewsModel()
//                model.title = it.title
//                model.link = it.url
//                model.imgUrl = it.img
//                cache.add(model)
//            }
//            view?.showMoreNews(cache)

            // блок комментариев
            if (Preferences.News.showNewsComments()) {
                val comments = await { Api.NewsApi().getComments(result.commentsBlock) }
                val _cache = ArrayList<NCModel>()
                comments.forEach {
                    val model = NCModel()
                    model.postId = it.commentId
                    model.username = it.commentData
                    model.text = it.commentText
                    model.postDate = it.commentData
                    _cache.add(model)
                }
                view?.showComments(_cache)
            }
        }
    }.onError {
        view?.showProgress(false)
        view?.showError()
    }

    init { setTag("NewsDetailsPresenterK") }
}
