package com.melon.android.tool;

import android.content.Context;
import android.content.SharedPreferences;

public class SpUtil {
    /**
     * 一般性配置	账号退出即消除
     */
    private static final String COMMON = "config";
    /**
     * 账号退出不会消除
     */
    public static final String UNCLEARABLE = "unclearable";

    /**
     * 默认sp
     */
    public static SharedPreferences getSharePerference(Context context) {
//		return context.getSharedPreferences(COMMON, Context.MODE_MULTI_PROCESS);
        return context.getSharedPreferences(COMMON, Context.MODE_PRIVATE);
    }

    /**
     * 获取其它sp
     * @param name sp配置文件名称
     */
    public static SharedPreferences getSharePerference(Context context, String name) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static void setInt(Context context, String name, String key, int value)	{
        getSharePerference(context,name).edit().putInt(key, value).commit();
    }
    public static int getInt(Context context, String name, String key, int defValue){
        return getSharePerference(context,name).getInt(key, defValue);
    }

    public static void setLong(Context context, String name, String key, long value)	{
        getSharePerference(context,name).edit().putLong(key, value).commit();
    }
    public static long getLong(Context context, String name, String key, long defValue){
        return getSharePerference(context,name).getLong(key, defValue);
    }

    public static void setString(Context context, String key, String value) {
        getSharePerference(context).edit().putString(key, value).commit();
    }
    public static String getString(Context context, String key) {
        return getSharePerference(context).getString(key, "");
    }

    public static void setBoolean(Context context, String key, boolean value) {
        getSharePerference(context).edit().putBoolean(key, value).commit();
    }
    public static boolean getBoolean(Context context, String key) {
        return getSharePerference(context).getBoolean(key, false);
    }

    public static void remove(Context context, String key) {
        getSharePerference(context).edit().remove(key).commit();
    }

}
