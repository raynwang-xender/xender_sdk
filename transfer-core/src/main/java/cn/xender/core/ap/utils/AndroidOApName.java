package cn.xender.core.ap.utils;

import android.text.TextUtils;

public class AndroidOApName {

    private static final String ANDROID_O_AP_NAME_FIX = "AndroidShare_";


    /**
     * ssid是不是android O 开的热点的名字
     * */
    public static boolean startWithAndroidOFix(String ssid) {
        if (TextUtils.isEmpty(ssid)) {
            return false;
        }

        if (ssid.startsWith("\"" + ANDROID_O_AP_NAME_FIX) || ssid.startsWith(ANDROID_O_AP_NAME_FIX)) {
            return true;
        }

        return false;
    }

}
