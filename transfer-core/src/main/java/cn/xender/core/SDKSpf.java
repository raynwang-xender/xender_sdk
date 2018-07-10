package cn.xender.core;

import android.content.Context;
import android.content.SharedPreferences;

public class SDKSpf {


    private static SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences("x_sdk_spf", Context.MODE_PRIVATE);
    }

    public static void putString(Context context,String key,String value){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();

        editor.putString(key,value);

        editor.apply();

    }

    public static void putBoolean(Context context,String key,boolean value){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();

        editor.putBoolean(key,value);

        editor.apply();
    }


    public static String getString(Context context,String key,String defaultValue){
        return getSharedPreferences(context).getString(key,defaultValue);
    }

    public static boolean getBoolean(Context context,String key,boolean defaultValue){
        return getSharedPreferences(context).getBoolean(key,defaultValue);
    }

}
