package forpdateam.ru.forpda.pref;

import forpdateam.ru.forpda.App;

/**
 * Created by isanechek on 5/11/17.
 */

public class Preferences {

    public static class News {
        public static void enableCompatItem(boolean enable) {
            App.getInstance().getPreferences().edit().putBoolean("news.list.compat", enable).apply();
        }

        public static boolean getCompatItem() {
            return App.getInstance().getPreferences().getBoolean("news.list.compat", false);
        }

        public static void enableTopCommentsNews(boolean enable) {
            App.getInstance().getPreferences().edit().putBoolean("news.list.top", enable).apply();
        }

        public static boolean getShowTopCommentsNew() {
            return App.getInstance().getPreferences().getBoolean("news.list.top", true);
        }

        public static void setTimeUpdateTopList(String category, long time) {
            App.getInstance().getPreferences().edit().putLong("news.top.list." + category, time).apply();
        }

        public static long getListTimeUpdateTop(String category) {
            return App.getInstance().getPreferences().getLong("news.top.list." + category, 0L);
        }
    }
}
