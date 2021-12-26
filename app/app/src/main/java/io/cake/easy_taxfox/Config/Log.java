package io.cake.easy_taxfox.Config;

/**
 * This class is used for logging purposes.
 */
public class Log {

    private static final boolean DEBUG = AppConfig.IS_DEBUG_MODE;

    public static void d(String tag, String message) {
        if(DEBUG) {
            android.util.Log.d(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if(DEBUG) {
            android.util.Log.e(tag, message);
        }
    }
}
