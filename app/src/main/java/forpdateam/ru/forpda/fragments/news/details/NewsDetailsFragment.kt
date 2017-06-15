package forpdateam.ru.forpda.fragments.news.details

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.extensions.loadImageFromNetwork
import forpdateam.ru.forpda.extensions.lparams
import forpdateam.ru.forpda.fragments.TabFragment
import forpdateam.ru.forpda.fragments.news.details.adapters.MoreNewsAdapter
import forpdateam.ru.forpda.fragments.news.details.adapters.NewsDetailsAdapter
import forpdateam.ru.forpda.fragments.news.details.presenter.NewsDetailsPresenter
import forpdateam.ru.forpda.models.news.MoreNewsModel
import forpdateam.ru.forpda.models.news.NCModel
import forpdateam.ru.forpda.models.news.NewsCommentModel
import forpdateam.ru.forpda.utils.ExtendedWebView
import forpdateam.ru.forpda.utils.Logger
import forpdateam.ru.forpda.views.FixCardView
import java.util.regex.Pattern

/**
 * Created by isanechek on 6/13/17.
 */
class NewsDetailsFragment : TabFragment(), INewsDetailsView {

    private var urlExt: String? = null
    private var imgUrlExt: String? = null
    private var titleExt: String? = null
    private var presenter: NewsDetailsPresenter? = null
    private var adapter: NewsDetailsAdapter? = null

    private lateinit var image: ImageView
    private lateinit var rootList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.log("onCreate")
        presenter = NewsDetailsPresenter()
        when {
            arguments != null -> {
                titleExt = arguments.getString(NewsDetailsFragment.TITLE)
                urlExt = arguments.getString(NewsDetailsFragment.NEWS_URL)
                imgUrlExt = arguments.getString(NewsDetailsFragment.IMG_URL)

            } else -> Logger.log("arguments empty")
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        hideToolbar()
        baseInflateFragment(inflater, R.layout.test_news_activity)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        image = findViewById(R.id.backdrop) as ImageView
        rootList = findViewById(R.id.newsDetailsList) as RecyclerView
        rootList.layoutManager = LinearLayoutManager(context)
        adapter = NewsDetailsAdapter()
        rootList.adapter = adapter
        presenter?.bind(this)
    }

    override fun onStart() {
        super.onStart()
        Logger.log("title s $titleExt")
        Logger.log("url s http:$urlExt")
        Logger.log("url s http:$imgUrlExt")
        presenter?.loadPage("http:$urlExt")
        image.loadImageFromNetwork("http:$imgUrlExt")

        val infoContainer = FixCardView(context)
        infoContainer.lparams(width = ViewGroup.LayoutParams.MATCH_PARENT)
        val title = TextView(context)
        title.lparams()
        title.text = "Title block"
        title.textSize = 16f
        infoContainer.addView(title)
        adapter?.addHeader(infoContainer)

    }

    override fun showProgress(show: Boolean) {
        Logger.log("showProgress $show")
    }

    override fun showWebViewContent(html: String?) {
        val container = FixCardView(context)
        container.lparams(width = ViewGroup.LayoutParams.MATCH_PARENT)
//        val webText = TextView(context)
//        webText.lparams(width = ViewGroup.LayoutParams.MATCH_PARENT)
//        webText.text = html
//        container.addView(webText)

        val webView = ExtendedWebView(context)
        webView.lparams(width = ViewGroup.LayoutParams.MATCH_PARENT)
        webView.loadDataWithBaseURL("http://4pda.ru/", html, "text/html", "utf-8", null)
        container.addView(webView)
        adapter?.addHeader(container)
    }

    override fun showMoreNews(list: List<MoreNewsModel>) {
        Logger.log("Show More News")
        val moreNewsList = RecyclerView(context)
        moreNewsList.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
        moreNewsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        moreNewsList.setHasFixedSize(true)
        val adapterMore = MoreNewsAdapter()
        Logger.log("More News Size ${list.size}")
        adapterMore.addAll(list)
        moreNewsList.adapter = adapterMore
        val container = FixCardView(context)
        container.lparams(width = ViewGroup.LayoutParams.MATCH_PARENT)
        container.addView(moreNewsList)
        adapter?.addHeader(container)

    }

    override fun showComments(comments: List<NCModel>) {
        Logger.log("show Comments")
        val cache = ArrayList<NewsCommentModel>()
        comments.mapTo(cache) { NewsCommentModel(it.text) }
        Logger.log("show coments cache ${cache.size}")
        adapter?.addAll(cache)
    }

    override fun showError() {
        Logger.log("Show Error View")
    }

    companion object {
        const val TITLE = "nd.title"
        const val IMG_URL = "nd.img.url"
        const val NEWS_URL = "nd.news.url"
    }

    init {
        Logger.setTag("NewsDetailsFragmentK")
        configuration.isUseCache = true
        configuration.isAlone = true
    }

}