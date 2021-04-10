package com.melon.android;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import com.melon.android.tool.WakeLockUtil;

public class WebActivity extends Activity {

    private MelonWebView mWebView;
    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mWebView = findViewById(R.id.wv_web);

        //关闭页面
        findViewById(R.id.tv_web_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String mUrl = getIntent().getStringExtra("url");
        mWebView.loadUrl(mUrl);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();// 返回前一个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        switch (config.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mWakeLock = WakeLockUtil.acquireWakeLock(getApplicationContext());
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                WakeLockUtil.release(mWakeLock);
                break;
            default:
                break;
        }
    }
}
