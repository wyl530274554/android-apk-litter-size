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

import com.melon.android.tool.ApiUtil;
import com.melon.android.tool.CommonUtil;
import com.melon.android.tool.Constant;
import com.melon.android.tool.HttpUtil;
import com.melon.android.tool.LogUtil;
import com.melon.android.tool.MelonConfig;
import com.melon.android.tool.SpUtil;
import com.melon.android.tool.SystemUtil;
import com.melon.android.tool.ToastUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.melon.android.tool.ApiUtil.API_APP_UPGRADE;
import static com.melon.android.tool.ApiUtil.APP_DOWNLOAD;

public class MainActivity extends Activity implements TextView.OnEditorActionListener, AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener {

    private final String[] mTitles = {"密码", "天气", "上海南站", "车墩站", "聊天", "笔记", "电话本"};
    private static final int ITEM_PASSWORD = 0;
    private static final int ITEM_WEATHER = 1;
    private static final int ITEM_SHNZ = 2; //金山铁路-上海南站
    private static final int ITEM_CHEDUN = 3; //金山铁路-车墩站
    private static final int ITEM_TALK = 4; //金山铁路-车墩站
    private EditText editText;
    private CheckBox cb_main_explorer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.et_main_search);
        editText.setOnEditorActionListener(this);

        CheckBox httpBox = findViewById(R.id.cb_main_http);
        CheckBox imageBox = findViewById(R.id.cb_main_image);
        cb_main_explorer = findViewById(R.id.cb_main_explorer);
        httpBox.setOnCheckedChangeListener(this);
        imageBox.setOnCheckedChangeListener(this);
        cb_main_explorer.setOnCheckedChangeListener(this);


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
        //服务器地址设置ip:port
        if (content.startsWith("ip://")) {
            String[] split = content.split("//");
            SpUtil.setString(this, "myServer", split[1]);
            ToastUtil.toast(this, "OK");
            return;
        }

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

        if (MelonConfig.isOpenInExplorer) {
            CommonUtil.enterBrowser(this, url);
        } else {
            CommonUtil.enterActivity(this, WebActivity.class, "url", url);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //无图
        //imageBox.setChecked(MelonConfig.isWebNoImage);

        // 全选
        editText.setText(editText.getText().toString());// 添加这句后实现效果
        editText.selectAll();

        //延时弹出键盘
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                editText.requestFocus();
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
            case ITEM_WEATHER:
                //查询天气
                CommonUtil.enterBrowser(this, Constant.URL_BAI_DU + "天气预报");
                break;
            case ITEM_SHNZ:
                //查询金山铁路-上海南站
                CommonUtil.enterBrowser(this, "http://www.shjstl.com/lately.php?station=%E4%B8%8A%E6%B5%B7%E5%8D%97%E7%AB%99");
                break;
            case ITEM_CHEDUN:
                //查询金山铁路-车墩站
                CommonUtil.enterBrowser(this, "http://www.shjstl.com/lately.php?station=%E8%BD%A6%E5%A2%A9");
                break;
            case ITEM_TALK:
                //聊天
                CommonUtil.enterBrowser(this, "http://"+ ApiUtil.API_IP +"/topic");
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
            case R.id.cb_main_explorer:
                MelonConfig.isOpenInExplorer = !isChecked;
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
