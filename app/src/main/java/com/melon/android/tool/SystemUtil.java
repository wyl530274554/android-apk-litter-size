package com.melon.android.tool;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * android系统相关工具
 */
public class SystemUtil {
    /**
     * 判断WIFI是否连接
     */
    public static boolean isWifiConnected(Context ctx) {
        ConnectivityManager manager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (info != null) {
            return info.isConnected();
        }
        return false;
    }
}
