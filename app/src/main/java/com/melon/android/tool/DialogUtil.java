package com.melon.android.tool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * 对话框处理
 *
 * @author melon.wang
 * @date 2019/1/17 16:49
 * Email 530274554@qq.com
 */
public class DialogUtil {
    /**
     * 普通的带确定/取消
     */
    public static void show(Activity activity, String message, DialogInterface.OnClickListener okListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message);
        builder.setTitle("提示");
        builder.setPositiveButton("确定", okListener);
        builder.setNegativeButton("取消", null);
        builder.show();
    }
}
