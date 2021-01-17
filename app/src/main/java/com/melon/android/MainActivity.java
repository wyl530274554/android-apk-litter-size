package com.melon.android;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.melon.android.tool.CommonUtil;
import com.melon.android.tool.Constant;
import com.melon.android.tool.HttpUtil;
import com.melon.android.tool.LogUtil;
import com.melon.android.tool.MelonConfig;
import com.melon.android.tool.SystemUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.melon.android.tool.ApiUtil.API_APP_UPGRADE;
import static com.melon.android.tool.ApiUtil.APP_DOWNLOAD;

public class MainActivity extends Activity implements TextView.OnEditorActionListener, AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener {

    private final String[] mTitles = {"密码"};
    private static final int ITEM_PASSWORD = 0;
    private EditText editText;
    private CheckBox imageBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.et_main_search);
        editText.setOnEditorActionListener(this);

        CheckBox httpBox = findViewById(R.id.cb_main_http);
        imageBox = findViewById(R.id.cb_main_image);
        httpBox.setOnCheckedChangeListener(this);
        imageBox.setOnCheckedChangeListener(this);


        GridView gridView = findViewById(R.id.gv_main);
        gridView.setAdapter(new MainAdapter());
        gridView.setOnItemClickListener(this);

        getAppUpgradeInfo();
    }

    private void getAppUpgradeInfo() {
        HttpUtil.doGet(this, API_APP_UPGRADE, new HttpUtil.HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                try {
                    LogUtil.d("getAppUpgradeInfo: " + response);
                    JSONObject jsonObject = new JSONObject(response);
                    int code = jsonObject.optInt("code");
                    String name = jsonObject.optString("name");
                    if (code > CommonUtil.getVersionCode(getApplicationContext())) {
                        //升级
                        CommonUtil.downFileBySystem(getApplicationContext(), APP_DOWNLOAD + name, name);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.d("getAppUpgradeInfo exception" + e.getMessage());
                }
            }

            @Override
            public void onError(Exception e) {
                LogUtil.e("getAppUpgradeInfo error : ", e.getMessage());
            }
        });
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            String content = textView.getText().toString().trim();
            if (!content.isEmpty()) {
                enterSearch(content);
            }
            return true;
        }
        return false;
    }

    private void enterSearch(String content) {
        String url;
        if (content.startsWith("http")) {
            url = content;
        } else {
            if (isHttp) {
                if (content.contains(".")) {
                    url = "http://" + content;
                } else {
                    url = "http://" + content + ".com";
                }
            } else {
                url = Constant.URL_BAI_DU + content;
            }
        }

        //若当前不是wifi网络，则直接使用系统默认浏览器
        if (SystemUtil.isWifiConnected(getApplicationContext())) {
            CommonUtil.enterActivity(this, WebActivity.class, "url", url);
        } else {
            CommonUtil.enterBrowser(this, url);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //无图
        imageBox.setChecked(MelonConfig.isWebNoImage);

        // 全选
        editText.setText(editText.getText().toString());// 添加这句后实现效果
        editText.selectAll();

        //延时弹出键盘
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                CommonUtil.hideInputMode(MainActivity.this, false);
            }
        }, 500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //隐藏键盘
        CommonUtil.hideInputMode(MainActivity.this, true);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        switch (position) {
            case ITEM_PASSWORD:
                //查询密码
                CommonUtil.enterFragment(this, CommonFragmentActivity.FRAGMENT_PASSWORD);
                break;
            default:
        }
    }

    boolean isHttp;

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_main_http:
                isHttp = isChecked;
                break;
            case R.id.cb_main_image:
                MelonConfig.isWebNoImage = isChecked;
                break;
            default:
        }
    }

    /**
     * 首页的网格布局适配器
     */
    class MainAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public Object getItem(int i) {
            return mTitles[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView tv = new TextView(viewGroup.getContext());
            tv.setTextSize(20);
            tv.setHeight(150);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.CENTER);
            tv.setText(mTitles[i]);
            return tv;
        }
    }
}
