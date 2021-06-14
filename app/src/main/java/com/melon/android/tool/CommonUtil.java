package com.melon.android.tool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.DownloadManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.melon.android.CommonFragmentActivity;
import com.melon.android.MelonApplication;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2016/5/17.
 */
public class CommonUtil {
    /**
     * 启动Activity
     *
     * @param ctx   上下文
     * @param clazz 要跳转页面
     */
    public static void enterActivity(Context ctx, Class<?> clazz) {
        enterActivity(ctx, clazz, null);
    }

    public static void enterActivity(Context ctx, Class<?> clazz, String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        enterActivity(ctx, clazz, map);
    }

    public static void enterActivity(Context ctx, Class<?> clazz, Map<String, String> params) {
        Intent intent = new Intent(ctx, clazz);
        if (ctx instanceof Application) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key != null && value != null) {
                    intent.putExtra(key, value);
                }
            }
        }
        ctx.startActivity(intent);
    }

    /**
     * 启动Fragment
     *
     * @param ctx    上下文
     * @param target CommonFragmentActivity中定义的常量 目标fragment
     */
    public static void enterFragment(Context ctx, int target) {
        Intent intent = new Intent(ctx, CommonFragmentActivity.class);
        intent.putExtra("target", target);
        if (ctx instanceof Application) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        ctx.startActivity(intent);
    }

    /**
     * 跳转至浏览器
     */
    public static void enterBrowser(Context ctx, String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        if (ctx instanceof Application) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        ctx.startActivity(intent);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int getScreenHeight(Context ctx) {
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getMetrics(metrics);
        return metrics.heightPixels;
    }

    public static int getScreenWidth(Context ctx) {
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getMetrics(metrics);
        return metrics.widthPixels;
    }

    /**
     * 通过反射获取状态栏高度，默认25dp
     */
    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = dip2px(context, 25);
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    //全屏
    public static void fullScreen(Activity act) {
        act.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static void cancelFullScreen(Activity act) {
        act.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 空判断
     */
    public static boolean isEmpty(CharSequence str) {
        if (str == null || str.length() == 0 || "null".equalsIgnoreCase(str.toString()))
            return true;
        else
            return false;
    }

    /**
     * 空判断
     */
    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    /**
     * 计算图片高度
     */
    public static int getPicHeight(Context ctx, int img) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeResource(ctx.getResources(), img, options);
        return options.outHeight;
    }

    /**
     * 复制到剪切板
     */
    public static void addToClipboard(Context ctx, String value) {
        ClipboardManager cmb2 = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb2.setText(value);
    }

    /**
     * 隐藏软键盘
     *
     * @param activity
     * @param flag     true    隐藏
     */
    public static void hideInputMode(Activity activity, boolean flag) {
        if (flag) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = activity.getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } else {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = activity.getCurrentFocus();
            if (view != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            }
        }
    }

    /**
     * 时间截转日期、时间
     *
     * @param time 秒
     */
    @SuppressLint("SimpleDateFormat")
    public static String getDateTime(String time) {
        String date = "";
        try {
            date = new SimpleDateFormat("MM-dd-yyyy  HH:mm:ss").format(new Date(Long.parseLong(time) * 1000)); // * 1000
        } catch (Exception e) {
            if (!isEmpty(time)) {
                return time;
            }
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 获取日常时间显示
     */
    public static String getCurrentDataTime() {
        return SimpleDateFormat.getDateTimeInstance().format(System.currentTimeMillis());
    }

    /**
     * 友好时间
     *
     * @param time 秒
     * @return
     */
    public static String getMyDateFormat(String time) {
        String result = "未知";
        try {
            long dataTime = Long.parseLong(time) * 1000;

            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
            String today = sdfDate.format(new Date());
            long zeroTime = sdfDate.parse(today).getTime(); // 今天0点，毫秒
            long dTime = zeroTime - dataTime; // 时间间隔
            if (dTime < 0) {
                result = "今天 " + sdfTime.format(dataTime);
            } else if (dTime < 86400000) {
                result = "昨天 " + sdfTime.format(dataTime);
            } else if (dTime < 86400000 * 6) {
                int day = (int) (dTime / 86400000);
                result = (day + 1) + "天前";
            } else if (dTime < 86400000 * 13) {
                result = "1周前";
            } else if (dTime < 86400000 * 20) {
                result = "2周前";
            } else if (dTime < 86400000 * 27L) {
                result = "3周前";
//            } else {
//                result = "更早";
            } else {
                result = getDateTime(time);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void setTransparentStateBar(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = act.getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            act.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * 手机号校验
     */
    public static boolean isMobileChina(String phone) {
        // 验证手机号
        Pattern p = Pattern.compile("^[1][3,4,5, 6,7,8][0-9]{9}$");
        Matcher m = p.matcher(phone);
        return m.matches();
    }

    /**
     * 网页分享
     *
     * @param context 上下文
     * @param url     分享内容
     */
    public static void shareWebUrl(Context context, String url) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        shareIntent.setType("text/plain");

        //设置分享列表的标题，并且每次都显示分享列表
        context.startActivity(Intent.createChooser(shareIntent, "Share To"));
    }

    public static String formatDataSize(long size) {
        DecimalFormat format = new DecimalFormat("####.00");
        if (size < 1024) {
            return size + "bytes";
        } else if (size < 1024 * 1024) {
            float kbSize = size / 1024f;
            return format.format(kbSize) + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            float mbSize = size / 1024f / 1024f;
            return format.format(mbSize) + "MB";
        } else {
            float gbSize = size / 1024f / 1024f / 1024f;
            return format.format(gbSize) + "GB";
        }
    }

    /**
     * 软件版本号
     */
    public static String getVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
        return pi.versionName + "_" + pi.versionCode;
    }

    /**
     * 软件版本Code
     */
    public static int getVersionCode(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
        return pi.versionCode;
    }

    /**
     * 系统自带文件下载
     *
     * @param url      地址
     * @param fileName 文件名
     */
    public static void downFileBySystem(Context context, String url, String fileName) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        //下载时，下载完成后显示通知
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //下载的路径，第一个参数是文件夹名称，第二个参数是下载的文件名
        request.setDestinationInExternalFilesDir(context, null, fileName);
        request.setVisibleInDownloadsUi(true);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
        }
    }

    /**
     * 全局上下文
     */
    public static Context getAppContext(){
        return MelonApplication.appContext;
    }
}
