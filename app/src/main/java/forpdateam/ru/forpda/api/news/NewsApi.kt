package forpdateam.ru.forpda.api.news

import android.util.Log

import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

import forpdateam.ru.forpda.api.Api
import forpdateam.ru.forpda.api.Utils
import forpdateam.ru.forpda.models.news.NewsNetworkModel
import io.reactivex.Single

import forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_ALL
import forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_ARTICLES
import forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_GAMES
import forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ACCESSORIES_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ACOUSTICS_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ANDROID_GAME
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ANDROID_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_DEVSTORY_GAMES
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_DEVSTORY_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_ANDROID
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_INTERVIEW
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_IOS
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_WP
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_IOS_GAME
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_IOS_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_NOTEBOOKS_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_SMARTPHONES_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_SMART_WATCH_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_TABLETS_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_WP7_GAME
import forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_WP7_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ACCESSORIES_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ACOUSTICS_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ALL
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ANDROID_GAME
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ANDROID_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ARTICLES
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_DEVSTORY_GAMES
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_DEVSTORY_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_GAMES
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_ANDROID
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_INTERVIEW
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_IOS
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_WP
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_IOS_GAME
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_IOS_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_NOTEBOOKS_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_SMARTPHONES_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_SMART_WATCH_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_SOFTWARE
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_TABLETS_REVIEWS
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_WP7_GAME
import forpdateam.ru.forpda.api.news.Constants.NEWS_URL_WP7_SOFTWARE
import forpdateam.ru.forpda.api.update.RegexStorage
import forpdateam.ru.forpda.utils.Logger
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Created by radiationx on 31.07.16.
 */
class NewsParser {

    /* Groups:
    *  1. Link
    * 2. Title
    * 3. Image Url
    * 4. Comments Count
    * 5. Date
    * 6. Author
    * 7. Description
     * */
    private val pattern = Pattern.compile("<article[^>]*?class=\"post\"[^>]*?data-ztm=\"[^ ]+\"[^>]*>[\\s\\S]*?<a[^>]*?href=\"([^\"]*)\"[^>]*?title=\"([^\"]*?)\"[\\s\\S]*?<img[^>]*?src=\"([^\"]*?)\"[\\s\\S]*?<a[^>]*?>([^<]*?)<\\/a>[\\s\\S]*?<em[^>]*?class=\"date\"[^>]*?>([^<]*?)<\\/em>[\\s\\S]*?<a[^>]*?>([^<]*?)<\\/a>[\\s\\S]*?<div[^>]*?itemprop=\"description\">([\\s\\S]*?)<\\/div>[\\s\\S]*?<\\/article>")

    private val newsListPattern = Pattern.compile(RegexStorage.News.List.getListPattern())
    private fun getNewsListFromNetwork2(category: String, pageNumber: Int): ArrayList<NewsNetworkModel> {
        val cache = ArrayList<NewsNetworkModel>()
        var url = getUrlCategory(category)
        if (pageNumber >= 2) {
            url = url + "page/" + pageNumber + "/"
        }

        Log.d("LOG", "getNewsListFromNetwork2 ->> $category $pageNumber $url")
        try {
            val response = Api.getWebClient().get(url) ?: return ArrayList()
            val matcher = newsListPattern.matcher(response)
            while (matcher.find()) {
                val model = NewsNetworkModel()
                model.link = matcher.group(1)
                model.imageUrl = matcher.group(3)
                model.title = Utils.fromHtml(matcher.group(2))
                model.commentsCount = matcher.group(4)
                model.date = matcher.group(5)
                model.author = Utils.fromHtml(matcher.group(6))
                model.description = Utils.fromHtml(matcher.group(7))
                model.category = category
                cache.add(model)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return cache
    }

    fun getNewsListFromNetwork1(category: String, pageNumber: Int): Single<ArrayList<NewsNetworkModel>> {
        return Single.fromCallable { getNewsListFromNetwork2(category, pageNumber) }
    }

    private fun getUrlCategory(category: String): String {
        when (category) {
            NEWS_CATEGORY_ALL -> return NEWS_URL_ALL
            NEWS_CATEGORY_ARTICLES -> return NEWS_URL_ARTICLES
            NEWS_CATEGORY_REVIEWS -> return NEWS_URL_REVIEWS
            NEWS_CATEGORY_SOFTWARE -> return NEWS_URL_SOFTWARE
            NEWS_CATEGORY_GAMES -> return NEWS_URL_GAMES
            NEWS_SUBCATEGORY_DEVSTORY_GAMES -> return NEWS_URL_DEVSTORY_GAMES
            NEWS_SUBCATEGORY_WP7_GAME -> return NEWS_URL_WP7_GAME
            NEWS_SUBCATEGORY_IOS_GAME -> return NEWS_URL_IOS_GAME
            NEWS_SUBCATEGORY_ANDROID_GAME -> return NEWS_URL_ANDROID_GAME
            NEWS_SUBCATEGORY_DEVSTORY_SOFTWARE -> return NEWS_URL_DEVSTORY_SOFTWARE
            NEWS_SUBCATEGORY_WP7_SOFTWARE -> return NEWS_URL_WP7_SOFTWARE
            NEWS_SUBCATEGORY_IOS_SOFTWARE -> return NEWS_URL_IOS_SOFTWARE
            NEWS_SUBCATEGORY_ANDROID_SOFTWARE -> return NEWS_URL_ANDROID_SOFTWARE
            NEWS_SUBCATEGORY_SMARTPHONES_REVIEWS -> return NEWS_URL_SMARTPHONES_REVIEWS
            NEWS_SUBCATEGORY_TABLETS_REVIEWS -> return NEWS_URL_TABLETS_REVIEWS
            NEWS_SUBCATEGORY_SMART_WATCH_REVIEWS -> return NEWS_URL_SMART_WATCH_REVIEWS
            NEWS_SUBCATEGORY_ACCESSORIES_REVIEWS -> return NEWS_URL_ACCESSORIES_REVIEWS
            NEWS_SUBCATEGORY_NOTEBOOKS_REVIEWS -> return NEWS_URL_NOTEBOOKS_REVIEWS
            NEWS_SUBCATEGORY_ACOUSTICS_REVIEWS -> return NEWS_URL_ACOUSTICS_REVIEWS
            NEWS_SUBCATEGORY_HOW_TO_ANDROID -> return NEWS_URL_HOW_TO_ANDROID
            NEWS_SUBCATEGORY_HOW_TO_IOS -> return NEWS_URL_HOW_TO_IOS
            NEWS_SUBCATEGORY_HOW_TO_WP -> return NEWS_URL_HOW_TO_WP
            NEWS_SUBCATEGORY_HOW_TO_INTERVIEW -> return NEWS_URL_HOW_TO_INTERVIEW
        }
        return NEWS_URL_ALL
    }

    /*Details*/

    fun getDetailsNews(html: String) : NewsItem {
        val pattern: Pattern = Pattern.compile(RegexStorage.News.Details.getRootDetailsPattern())
        val matcher: Matcher = pattern.matcher(html)
        var contentBlock = ""
        var moreNewsBlock = ""
        var navigationBlock = ""
        var count = ""
        var commentsBlock = ""
        while (matcher.find()) {
            contentBlock = matcher.group(1)
            moreNewsBlock = matcher.group(2)
            navigationBlock = matcher.group(3)
            count = matcher.group(4)
            commentsBlock = matcher.group(5)
        }
        return NewsItem(contentBlock, moreNewsBlock, navigationBlock, count, commentsBlock)
    }


    fun getDetailsPageContent(html: String?) : String? {
        val pattern = RegexStorage.News.Details.getContentPattern().toRegex()
        return html?.let { pattern.matchEntire(it)?.groups?.get(1)?.value }
    }

    fun getDetailsMoreNews(html: String) : String? {
        val pattern = RegexStorage.News.Details.getMoreNewsPattern().toRegex()
        return pattern.matchEntire(html)?.groups?.get(1)?.value
    }

    fun getDetailsMoreNewsJ(html: String) : ArrayList<NewsMore> {
        val cache = ArrayList<NewsMore>()
        val doc: Document = Jsoup.parse(html)
        val elements = doc
                .body()
                .getElementsByClass("materials-box")
                .first()
                .children()
        for (i in 0..elements.size -1) {
            val element = elements[i] as Element
            if (element.tagName() == "ul") {
                val url = element.select("a").attr("href").replace("?utm_source=thematic1", "")
                val title = element.select("a").attr("title")
                val img = element.select("img").attr("src")
                cache.add(NewsMore(title, url, img))
            }
        }
        Logger.log("getDetailsMoreNewsJ size cache ${cache.size}")
        return cache
    }

    fun getDetailsNavigation(html: String) : String? {
        val pattern = RegexStorage.News.Details.getNavigationPattern().toRegex()
        return pattern.matchEntire(html)?.groups?.get(1)?.value
    }

    fun getComments(source: String) : ArrayList<Comment> {
        val cache = ArrayList<Comment>()
        val doc: Document = Jsoup.parse(source)
        val element = doc.body()

        (0..element.children().size -1)
                .map { element.child(it) }
                .forEach {
                    var nickname = ""
                    var userUrl = ""
                    var commentId = ""
                    var commentDate = ""
                    var commentText = ""
                    var commentReplay = ""
                    when {
                        it.tagName().contains("div") -> for (j in 0..it.children().size -1) {
                            val ee = it.child(j)
                            when {
                                ee.getElementsByClass("heading") != null -> {
                                    val heading = it.getElementsByClass("heading").first()
                                    userUrl = heading.getElementsByClass("nickname").first().attr("href")
                                    nickname = heading.getElementsByClass("nickname").first().text()
                                    commentDate = heading.getElementsByClass("h-meta").first().text()
                                    commentId = heading.getElementsByClass("h-meta").first().select("a").attr("data-report-comment")
                                }
                                ee.tagName().contains("p") -> commentText = ee.html()
                            }
                        }
                        it.tagName().contains("ul") -> if (it.children().isNotEmpty()) {
                            commentReplay = it.toString()
                        }
                    }
                    cache.add(Comment(nickname, userUrl, commentId, commentDate, commentText, commentReplay))
                }
        Logger.log("getComments size ${cache.size}")
        return cache
    }

    init { Logger.setTag("NewsApi") }

}

class Comment(val nickname: String,
              val userUrl: String,
              val commentId: String,
              val commentData: String,
              val commentText: String,
              val commentReplay: String)

class NewsItem(val content: String,
              val moreNews: String,
              val navigation: String,
              val commentsCount: String,
              val commentsBlock: String)

class NewsMore(val title: String,
               val url: String,
               val img: String)