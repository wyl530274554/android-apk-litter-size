package com.melon.android.tool;

import android.content.Context;
import android.os.PowerManager;

/**
 * 亮屏
 */
public class WakeLockUtil {
    public static PowerManager.WakeLock acquireWakeLock(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm == null)
            return null;
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.FULL_WAKE_LOCK |
                        PowerManager.ON_AFTER_RELEASE,
                context.getClass().getName());
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        return wakeLock;
    }

    public static void release(PowerManager.WakeLock wakeLock) {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
