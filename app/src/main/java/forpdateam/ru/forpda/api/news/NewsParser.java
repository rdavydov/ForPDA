package forpdateam.ru.forpda.api.news;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.models.news.NewsNetworkModel;
import io.reactivex.Single;

import static forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_ALL;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_ARTICLES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_GAMES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_CATEGORY_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ACCESSORIES_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ACOUSTICS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ANDROID_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_ANDROID_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_DEVSTORY_GAMES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_DEVSTORY_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_ANDROID;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_INTERVIEW;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_IOS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_HOW_TO_WP;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_IOS_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_IOS_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_NOTEBOOKS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_SMARTPHONES_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_SMART_WATCH_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_TABLETS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_WP7_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_SUBCATEGORY_WP7_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ACCESSORIES_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ACOUSTICS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ALL;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ANDROID_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ANDROID_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_ARTICLES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_DEVSTORY_GAMES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_DEVSTORY_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_GAMES;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_ANDROID;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_INTERVIEW;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_IOS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_HOW_TO_WP;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_IOS_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_IOS_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_NOTEBOOKS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_SMARTPHONES_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_SMART_WATCH_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_SOFTWARE;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_TABLETS_REVIEWS;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_WP7_GAME;
import static forpdateam.ru.forpda.api.news.Constants.NEWS_URL_WP7_SOFTWARE;

/**
 * Created by radiationx on 31.07.16.
 */
public class NewsParser {
    /* Groups:
    * 1. Link
    * 2. Title
    * 3. Image Url
    * 4. Comments Count
    * 5. Date
    * 6. Author
    * 7. Description */
    private static final Pattern pattern = Pattern.compile("<article[^>]*?class=\"post\"[^>]*?data-ztm=\"[^ ]+\"[^>]*>[\\s\\S]*?<a[^>]*?href=\"([^\"]*)\"[^>]*?title=\"([^\"]*?)\"[\\s\\S]*?<img[^>]*?src=\"([^\"]*?)\"[\\s\\S]*?<a[^>]*?>([^<]*?)<\\/a>[\\s\\S]*?<em[^>]*?class=\"date\"[^>]*?>([^<]*?)<\\/em>[\\s\\S]*?<a[^>]*?>([^<]*?)<\\/a>[\\s\\S]*?<div[^>]*?itemprop=\"description\">([\\s\\S]*?)<\\/div>[\\s\\S]*?<\\/article>");

    /* Groups:
    * 1. Content source
    * 2. Materials items source
    * 3. Magic id for newer/older navigation
    * 4. Comments Count
    * 5. Comments tree source */
    private static final Pattern newPattern = Pattern.compile("<div class=\"content-box\" itemprop=\"articleBody\"[^>]*?>([\\s\\S]*?)<\\/div>[^<]*?<\\/div>[^<]*?<\\/div>[^<]*?<div class=\"materials-box\"[^>]*?>[\\s\\S]*?<ul class=\"materials-slider\"[^>]*?>([\\s\\S]*?)<\\/ul>[^<]*?<\\/div>[^<]*?<ul class=\"page-nav[^\"]*?\">[\\s\\S]*?<a href=\"[^\"]*?\\/(\\d+)\\/\"[\\s\\S]*?<\\/ul>[\\s\\S]*?<div class=\"comment-box\" id=\"comments\"[^>]*?>[^<]*?<div class=\"heading\"[^>]*?>[^>]*?<h2>(\\d+)[^<]*?<\\/h2>[\\s\\S]*?(<ul[\\s\\S]*?<\\/ul>)[^<]*?<form");

    /* Groups:
    * 1. News id
    * 2. Image url
    * 3. Title */
    private static final Pattern materialsPattern = Pattern.compile("<li class=\"slider-item\"[^>]*?>[^<]*?<a href=\"[^\"]*?\\/(\\d+)\\/\\?[^\"]*?\"[^>]*?><img src=\"([^\"]*?)\"[^>]*?>[^<]*?<\\/a>[^<]*?<h3>[^<]*?<a[^>]*?>([\\s\\S]*?)<\\/a>[^<]*?<\\/h3>");

    private ArrayList<NewsNetworkModel> getNewsListFromNetwork2(@NonNull String category, int pageNumber) {
        ArrayList<NewsNetworkModel> cache = new ArrayList<>();
        String url = getUrlCategory(category);
        if (pageNumber >= 2) { url = url + "page/" + pageNumber + "/"; }

        Log.d("LOG","getNewsListFromNetwork2 ->> " + category + " " + pageNumber + " " + url);
        try {
            String response = Api.getWebClient().get(url);
            if (response == null) return new ArrayList<>();
            Matcher matcher = pattern.matcher(response);
            while (matcher.find()) {
                NewsNetworkModel model = new NewsNetworkModel();
                model.link = matcher.group(1);
                model.imageUrl = matcher.group(3);
                model.title = Utils.fromHtml(matcher.group(2));
                model.commentsCount = matcher.group(4);
                model.date = matcher.group(5);
                model.author = Utils.fromHtml(matcher.group(6));
                model.description = Utils.fromHtml(matcher.group(7));
                model.category = category;
                cache.add(model);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cache;
    }

    public Single<ArrayList<NewsNetworkModel>> getNewsListFromNetwork1(@NonNull String category, int pageNumber) {
        return Single.fromCallable(() -> getNewsListFromNetwork2(category, pageNumber));
    }

    private String getUrlCategory(@NonNull String category) {
        switch (category) {
            case NEWS_CATEGORY_ALL:
                return NEWS_URL_ALL;
            case NEWS_CATEGORY_ARTICLES:
                return NEWS_URL_ARTICLES;
            case NEWS_CATEGORY_REVIEWS:
                return NEWS_URL_REVIEWS;
            case NEWS_CATEGORY_SOFTWARE:
                return NEWS_URL_SOFTWARE;
            case NEWS_CATEGORY_GAMES:
                return NEWS_URL_GAMES;
            case NEWS_SUBCATEGORY_DEVSTORY_GAMES:
                return NEWS_URL_DEVSTORY_GAMES;
            case NEWS_SUBCATEGORY_WP7_GAME:
                return NEWS_URL_WP7_GAME;
            case NEWS_SUBCATEGORY_IOS_GAME:
                return NEWS_URL_IOS_GAME;
            case NEWS_SUBCATEGORY_ANDROID_GAME:
                return NEWS_URL_ANDROID_GAME;
            case NEWS_SUBCATEGORY_DEVSTORY_SOFTWARE:
                return NEWS_URL_DEVSTORY_SOFTWARE;
            case NEWS_SUBCATEGORY_WP7_SOFTWARE:
                return NEWS_URL_WP7_SOFTWARE;
            case NEWS_SUBCATEGORY_IOS_SOFTWARE:
                return NEWS_URL_IOS_SOFTWARE;
            case NEWS_SUBCATEGORY_ANDROID_SOFTWARE:
                return NEWS_URL_ANDROID_SOFTWARE;
            case NEWS_SUBCATEGORY_SMARTPHONES_REVIEWS:
                return NEWS_URL_SMARTPHONES_REVIEWS;
            case NEWS_SUBCATEGORY_TABLETS_REVIEWS:
                return NEWS_URL_TABLETS_REVIEWS;
            case NEWS_SUBCATEGORY_SMART_WATCH_REVIEWS:
                return NEWS_URL_SMART_WATCH_REVIEWS;
            case NEWS_SUBCATEGORY_ACCESSORIES_REVIEWS:
                return NEWS_URL_ACCESSORIES_REVIEWS;
            case NEWS_SUBCATEGORY_NOTEBOOKS_REVIEWS:
                return NEWS_URL_NOTEBOOKS_REVIEWS;
            case NEWS_SUBCATEGORY_ACOUSTICS_REVIEWS:
                return NEWS_URL_ACOUSTICS_REVIEWS;
            case NEWS_SUBCATEGORY_HOW_TO_ANDROID:
                return NEWS_URL_HOW_TO_ANDROID;
            case NEWS_SUBCATEGORY_HOW_TO_IOS:
                return NEWS_URL_HOW_TO_IOS;
            case NEWS_SUBCATEGORY_HOW_TO_WP:
                return NEWS_URL_HOW_TO_WP;
            case NEWS_SUBCATEGORY_HOW_TO_INTERVIEW:
                return NEWS_URL_HOW_TO_INTERVIEW;
        }
        return NEWS_URL_ALL;
    }

    public static String getDetailsNewsItem(@NonNull String url) throws Exception {
        return Api.getWebClient().get(url);
    }

    public static Single<String> getDetailsSingle(String url) {
        return Single.fromCallable(() -> getDetailsNewsItem(url));
    }
}
