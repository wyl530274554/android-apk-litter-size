package com.melon.android;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.melon.android.tool.CommonUtil;
import com.melon.android.tool.Constant;
import com.melon.android.tool.DialogUtil;
import com.melon.android.tool.LogUtil;
import com.melon.android.tool.MelonConfig;

/**
 * WebView封装
 *
 * @author Melon
 */
public class MelonWebView extends WebView implements View.OnLongClickListener {
    private Context mContext;
    private String url;

    public MelonWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        initView();
        initWebParams();
    }

    private void initView() {
        this.setOnLongClickListener(this);
    }

    @SuppressLint({"ObsoleteSdkInt", "SetJavaScriptEnabled"})
    private void initWebParams() {
        setWebViewClient(new MelonWebViewClient());
        setWebChromeClient(new MelonWebChromeClient());
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Https嵌套http图片问题
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        //图片
        settings.setBlockNetworkImage(MelonConfig.isWebNoImage);

        //支持下载
        setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, String userAgent, final String contentDisposition, String mimetype, long contentLength) {
                // 调用系统下载
                //userAgent: Mozilla/5.0 (Linux; Android 8.0.0; SM-G9650 Build/R16NW; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/70.0.3538.110 Mobile Safari/537.36, contentDisposition: attachment; filename="手机淘宝.apk", mimetype: application/vnd.android.package-archive, contentLength: 178734468

                //非apk下载，跳转至浏览器
                if (!"application/vnd.android.package-archive".equals(mimetype)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse(url));
                    mContext.startActivity(intent);
                    return;
                }

                if (!(mContext instanceof Activity)) {
                    throw new RuntimeException("不是Activity不能开网页");
                }

                //下载文件名
                String fileName = System.currentTimeMillis() + ".apk";
                if (!TextUtils.isEmpty(contentDisposition) && contentDisposition.contains("filename=")) {
                    String[] split = contentDisposition.split("filename=");
                    fileName = split[1].substring(0, split[1].indexOf(".apk")).replace("\"", "") + ".apk";
                }

                LogUtil.d("fileName： " + fileName);

                //弹出确认对话框
                String finalFileName = fileName;
                DialogUtil.show((Activity) mContext, CommonUtil.formatDataSize(contentLength) + "\n确定下载吗？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CommonUtil.downFileBySystem(mContext, url, finalFileName);
                    }
                });
            }
        });
    }


    @Override
    public boolean onLongClick(View view) {
        HitTestResult result = ((WebView) view).getHitTestResult();
        if (null == result) {
            return false;
        }
        int type = result.getType();
        String extra = result.getExtra();
        LogUtil.d("type: " + type + ", extra: " + extra);
        switch (type) {
            case HitTestResult.EDIT_TEXT_TYPE:
                // 选中的文字类型
                break;

            case HitTestResult.PHONE_TYPE:
                // 处理拨号
                break;

            case HitTestResult.EMAIL_TYPE:
                // 处理Email
                break;

            case HitTestResult.GEO_TYPE:
                // 地图类型
                break;

            case HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                // 带有链接的图片类型
            case HitTestResult.IMAGE_TYPE:
                // 处理长按图片的菜单项
            case HitTestResult.SRC_ANCHOR_TYPE:
                // 超链接
                // 另起一页
                if (!CommonUtil.isEmpty(extra)) {
                    openNewWindow(extra, mContext);
                }
                break;
            //未知
            case HitTestResult.UNKNOWN_TYPE:
                return false;
            default:
        }
        return true;
    }

    /**
     * 开启新窗口
     */
    private void openNewWindow(String url, Context cxt) {
        CommonUtil.enterActivity(cxt, WebActivity.class, "url", url);
    }

    public String getCurrentUrl() {
        return url;
    }

    private class MelonWebChromeClient extends WebChromeClient {
        private View videoView;
        private CustomViewCallback mCustomViewCallback;

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (videoView != null) {
                callback.onCustomViewHidden();
                return;
            }
            videoView = view;

            ((ViewGroup) getParent()).addView(videoView);
            mCustomViewCallback = callback;
            setVisibility(View.GONE);
            ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        @Override
        public void onHideCustomView() {
            setVisibility(View.VISIBLE);
            if (videoView == null) {
                return;
            }
            videoView.setVisibility(View.GONE);
            ((ViewGroup) getParent()).removeView(videoView);
            mCustomViewCallback.onCustomViewHidden();
            videoView = null;
            ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            super.onHideCustomView();
        }
    }

    /**
     * 自己业务的WebViewClient
     */
    private class MelonWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(final WebView view, String url) {
            super.onPageFinished(view, url);
            LogUtil.d("onPageFinished: " + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            LogUtil.d("old loading: " + url);
            return super.shouldOverrideUrlLoading(view, url);
        }

        /*上面这个过时的是5.0以下可用；下面这个是5.0以上可用。
         *如果测试机是8.0系统，当这两个都存在时，只会调用下面的；而如果只存在上面的，也会调用上面的。(很神奇，很智能)
         *所以暂时可以只处理上面的
         */

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            url = request.getUrl().toString();
            LogUtil.d("shouldOverrideUrlLoading url: " + url);
            //电话处理
            if (url.startsWith(Constant.PROTOCOL_TEL)) {
                mContext.startActivity(new Intent(Intent.ACTION_DIAL, uri));
                return true;
            }
            //非http请求处理
            if (!url.startsWith(Constant.NET_PROTOCOL_HTTP)) {
                return true;
            }
            return super.shouldOverrideUrlLoading(view, request);
        }
    }
}
