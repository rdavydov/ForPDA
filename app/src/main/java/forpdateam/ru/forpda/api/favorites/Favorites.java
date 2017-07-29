package forpdateam.ru.forpda.api.favorites;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.NetworkRequest;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.favorites.models.FavData;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import forpdateam.ru.forpda.api.others.pagination.Pagination;

/**
 * Created by radiationx on 22.09.16.
 */

public class Favorites {
    private final static Pattern mainPattern = Pattern.compile("<div data-item-fid=\"([^\"]*)\" data-item-track=\"([^\"]*)\" data-item-pin=\"([^\"]*)\">[\\s\\S]*?(?:class=\"modifier\">(<font color=\"([^\"]*)\">|)([^< ]*)(<\\/font>|)<\\/span>)?<a href=\"[^\"]*=(\\d*)[^\"]*?\"[^>]*?>(<strong>|)([^<]*)(<\\/strong>|)<\\/a>(?: &nbsp;[\\s\\S]*?(<a href=\"[^\"]*=(\\d*)\">\\((\\d*?)\\)[^<]*?<\\/a>|)<\\/div>[\\s\\S]*?topic_desc\">([^<]*|)(<br[^>]*>|)[\\s\\S]*?showforum=([^\"]*?)\">([^<]*)<\\/a><br[^>]*>[\\s\\S]*?showuser=([^\"]*)\">([^<]*)<\\/a>[\\s\\S]*?showuser=([^\"]*)\">([^<]*)<\\/a> ([^<]*?)<|[^<]*?<\\/div>[^<]*?<div class=\"board-forum-lastpost[\\s\\S]*?<div class=\"topic_body\">([^<]*?) <a href=\"[^\"]*?(\\d+)\"[^>]*?>([^<]*?)<\\/a>)?");
    private final static Pattern checkPattern = Pattern.compile("<div style=\"[^\"]*background:#dff0d8[^\"]*\">[\\s\\S]*<div id=\"navstrip");
    private final static Pattern pagesPattern = Pattern.compile("parseInt\\((\\d*)\\)[\\s\\S]*?parseInt\\(st\\*(\\d*)\\)[\\s\\S]*?pagination\">[\\s\\S]*?<span[^>]*?>([^<]*?)<\\/span>");
    public final static int ACTION_EDIT_SUB_TYPE = 0;
    public final static int ACTION_EDIT_PIN_STATE = 1;
    public final static int ACTION_DELETE = 2;
    public final static int ACTION_ADD = 3;
    public final static String[] SUB_TYPES = {"none", "delayed", "immediate", "daily", "weekly", "pinned"};
    public final static CharSequence[] SUB_NAMES = {"Не уведомлять", "Первый раз", "Каждый раз", "Каждый день", "Каждую неделю", "При изменении первого поста"};

    public FavData getFavorites(int st) throws Exception {
        FavData data = new FavData();
        NetworkResponse response = Api.getWebClient().get("http://4pda.ru/forum/index.php?act=fav&st=".concat(Integer.toString(st)));
        long time = System.currentTimeMillis();
        Matcher matcher = mainPattern.matcher(response.getBody());
        FavItem item;
        while (matcher.find()) {
            item = new FavItem();
            boolean isForum = matcher.group(24) != null;

            item.setFavId(Integer.parseInt(matcher.group(1)));
            item.setTrackType(matcher.group(2));
            item.setPin(matcher.group(3).equals("1"));

            if (isForum) {
                item.setForumId(Integer.parseInt(matcher.group(8)));
            } else {
                if (!matcher.group(4).isEmpty())
                    item.setInfoColor(matcher.group(5));
                item.setInfo(matcher.group(6));
                item.setTopicId(Integer.parseInt(matcher.group(8)));
            }
            item.setNewMessages(!matcher.group(9).isEmpty());
            item.setTopicTitle(Utils.fromHtml(matcher.group(10)));

            if (isForum) {
                item.setDate(matcher.group(24));
                item.setLastUserId(Integer.parseInt(matcher.group(25)));
                item.setLastUserNick(Utils.fromHtml(matcher.group(26)));
                item.setForum(true);
            } else {
                if (!matcher.group(12).isEmpty()) {
                    item.setStParam(Integer.parseInt(matcher.group(13)));
                    item.setPages(Integer.parseInt(matcher.group(14)));
                }
                if (!matcher.group(15).isEmpty())
                    item.setDesc(Utils.fromHtml(matcher.group(15)));
                item.setForumId(Integer.parseInt(matcher.group(17)));
                item.setForumTitle(Utils.fromHtml(matcher.group(18)));
                item.setAuthorId(Integer.parseInt(matcher.group(19)));
                item.setAuthorUserNick(Utils.fromHtml(matcher.group(20)));
                item.setLastUserId(Integer.parseInt(matcher.group(21)));
                item.setLastUserNick(Utils.fromHtml(matcher.group(22)));
                item.setDate(matcher.group(23));
            }

            data.addItem(item);
        }
        data.setPagination(Pagination.parseForum(response.getBody()));
        Log.d("FORPDA_LOG", "parsing time " + ((System.currentTimeMillis() - time)));

        return data;
    }

    public boolean editSubscribeType(String type, int favId) throws Exception {
        NetworkResponse response = Api.getWebClient().get("http://4pda.ru/forum/index.php?act=fav&sort_key=&sort_by=&type=all&st=0&tact=" + type + "&selectedtids=" + favId);
        return checkIsComplete(response.getBody());
    }

    public boolean editPinState(String type, int favId) throws Exception {
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=fav")
                .formHeader("selectedtids", Integer.toString(favId))
                .formHeader("tact", type);
        NetworkResponse response = Api.getWebClient().request(builder.build());
        return checkIsComplete(response.getBody());
    }

    public boolean delete(int favId) throws Exception {
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=fav")
                .xhrHeader()
                .formHeader("selectedtids", Integer.toString(favId))
                .formHeader("tact", "delete");
        NetworkResponse response = Api.getWebClient().request(builder.build());
        return checkIsComplete(response.getBody());
    }

    public boolean add(int id, String type) throws Exception {
        NetworkResponse response = Api.getWebClient().request(new NetworkRequest.Builder().url("http://4pda.ru/forum/index.php?act=fav&type=add&t=" + id + "&track_type=" + type).build());
        return checkIsComplete(response.getBody());
    }

    private boolean checkIsComplete(String result) {
        //forpdateam.ru.forpda.utils.Utils.longLog("FAVORITE RESULT "+result);
        boolean res = checkPattern.matcher(result).find();
        Log.d("FORPDA_LOG", "checkIsComplete " + res);
        return res;
    }
}
