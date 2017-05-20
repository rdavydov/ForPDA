package forpdateam.ru.forpda.utils;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by isanechek on 5/20/17.
 */

public class Logger {

    private static String ROOT_TAG = "forpda";

    public static void setTag(@NonNull String tag) {
        ROOT_TAG = ROOT_TAG + " " + tag;
    }

    public static void log(@NonNull String msg) {
        Log.e(ROOT_TAG, msg);
    }
}
