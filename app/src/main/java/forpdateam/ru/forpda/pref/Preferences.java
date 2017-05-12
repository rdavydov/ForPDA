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
    }
}
