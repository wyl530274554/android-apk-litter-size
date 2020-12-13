package com.melon.android.tool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtil {

    /**
     * 在子线程中弹出短时间Toast
     */
    public static void showSubThreadShortToast(final Activity activity, final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 在子线程中弹出长时间Toast
     */
    public static void showSubThreadLongToast(final Activity activity, final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 短时间Toast显示
     */
    public static void toast(Context context, String msg) {
        if (context == null) return;
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
//        View view = LayoutInflater.from(context).inflate(R.layout.my_toast, null);
        toast.show();
    }

    public static void showLongToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * 设计原理：在Toast显示消失之前，再次调用Toast.show()进行接力。
     */
    public static class CustomToast {
        private static Toast toast = null;
        private static Handler handler = null;
        private static boolean isToastRunning = false; // 防止重复点击
        private static Runnable toastShowTask = new Runnable() {
            public void run() {
                toast.show();
                handler.postDelayed(toastShowTask, 3300);// 若此处为4s，则会断断续续显示
            }
        };

        /**
         * 显示TOAST
         *
         * @param context 上下文
         * @param msg     提示消息
         * @param time    显示时长
         */
        @SuppressLint("ShowToast")
        public static void show(Context context, String msg, final long time) {
            if (isToastRunning) {
                return;
            }
            handler = new Handler(context.getMainLooper());
            toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            // 显示
            handler.post(toastShowTask);
            isToastRunning = true;
            // 取消
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 删除Handler队列中的仍处理等待的消息
                    handler.removeCallbacks(toastShowTask);
                    // 取消仍在显示的Toast
                    toast.cancel();
                    isToastRunning = false;
                }
            }, time);
        }
    }
}
