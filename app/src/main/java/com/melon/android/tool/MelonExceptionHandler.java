package com.melon.android.tool;

import android.content.Context;
import android.content.pm.PackageInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MelonExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Context mContext;

    public MelonExceptionHandler(Context context) {
        this.mContext = context;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        recordException(e, mContext);
        // 结束应用
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void recordException(Throwable ex, Context context) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);

        String str = sw.toString();
        try {
            StringBuilder sb = new StringBuilder();

            String dirPath = context.getExternalFilesDir("") + "";
            File file = new File(dirPath, "error.txt");
            FileOutputStream fos = new FileOutputStream(file, false);

            sb.append(str);

            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int versionCode = packageInfo.versionCode;
            String versionName = packageInfo.versionName;
            sb.append("\n\nVersionCode: ").append(versionCode).append("\n");
            sb.append("VersionName: ").append(versionName).append("\n");
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
            sb.append("ErrorTime: " + time + "\n\n\n\n\n");

            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
