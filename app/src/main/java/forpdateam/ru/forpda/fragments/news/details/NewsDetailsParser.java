package forpdateam.ru.forpda.fragments.news.details;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Function;
import com.annimon.stream.function.Predicate;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.models.news.MoreNewsModel;
import forpdateam.ru.forpda.models.news.NCModel;

import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by isanechek on 5/8/17.
 */

public class NewsDetailsParser {

    public static Element getRootElement(String page) {
        Document doc = Jsoup.parse(page);
        Element body = doc.body();
        return body.select("article").first();
    }

    public static String getContent(Element container) {
        String result = null;
        String html = container
                .getElementsByClass("container")
                .first()
                .getElementsByClass("content")
                .html();

        result = "<!DOCTYPE html>" +
                "<html><head>" +
                "<title> Hello </title>" +
                "</head><body>" +
                html +
                "</body>" +
                "</html>";
        return result;
    }

    public static List<MoreNewsModel> getMoreNews(Element element) {
        Element more = element.getElementsByClass("materials-box").first();
        List<MoreNewsModel> cache = new ArrayList<>();
        Stream.of(more.select("materials-box > ul > li > a"))
                .filter(value -> value != null)
                .map(element1 -> {
                    MoreNewsModel model = new MoreNewsModel();
                    model.link = element1.attr("href").replace("?utm_source=thematic1", "");
                    model.title = element1.attr("title");
                    model.imgUrl = element1.select("img").attr("src");
                    return model;
                })
                .forEach(cache::add);
        return cache;
    }

    public static List<String> getNextPrevNewsLink(Element element) {
        List<String> list = new ArrayList<>();
        Stream.of(element.getElementsByClass("page-nav box").first())
                .filter(value -> value != null)
                .map(e -> e.select("li > a").attr("href"))
                .filter(s -> s != null)
                .forEach(list::add);
        return list;
    }

    public static List<NCModel> getComments(Element element) {
        List<NCModel> cache = new ArrayList<>();
        Stream.of(element.getElementsByClass("comment-box")
                .first()
                .select("ul")
                .first()
                .children())
                .map(NewsDetailsParser::getComment)
                .forEach(cache::add);
        return cache;
    }

    private static NCModel getComment(Element comment) {
        NCModel model = new NCModel();

        for (Element c : comment.children()) {
            if (c.tagName().contains("div")) {
                for (Element dd : c.children()) {
                    if (dd.getElementsByClass("heading") != null) {
                        Element heading = comment.getElementsByClass("heading").first();
                        model.userProfUrl = heading.getElementsByClass("nickname").first().attr("href");
                        model.username = heading.getElementsByClass("nickname").first().text();
                        model.postDate = heading.getElementsByClass("h-meta").first().text();
                        String id = heading.getElementsByClass("h-meta").first().select("a").attr("data-report-comment");
                        model.postId = Integer.parseInt(id);
                    }

                    if (dd.tagName().contains("p")) {
                        model.text = dd.html();
                    }
                }
            }

            if (c.tagName().contains("ul")) {
                if (!c.children().isEmpty()) {
                    model.reply = c.html();
                }
            }
        }
        return model;
    }
}
