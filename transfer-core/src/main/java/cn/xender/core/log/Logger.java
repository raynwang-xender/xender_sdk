package cn.xender.core.log;

import android.util.Log;

import cn.xender.core.DevConfig;


public class Logger implements DevConfig {

	/*
	 * for debug true or false
	 */
	public static boolean r = isDebug;


	public static void c(String tag, String message) {
		if (r) {
			Log.d(tag, message);
		}
	}

    public static void ce(String tag, String message) {
        if (r) {
            Log.e(tag, message);
        }
    }

	public static void d(String tag, String message) {
		if (r) {
			Log.d(tag, message);
		}
	}

	public static void i(String tag, String message) {
		if (r) {
			Log.i(tag, message);
		}

	}

	public static void e(String tag, String message) {
		if (r) {
			Log.e(tag, message);
		}

	}

	public static void d(String tag, String msg, Throwable tr) {

		if(r){
			 Log.d(tag, msg, tr);
		}

	}


	public static void e(String tag, String msg, Throwable tr) {
		if (r) {
			 Log.e(tag, msg, tr);
		}
	}

}
