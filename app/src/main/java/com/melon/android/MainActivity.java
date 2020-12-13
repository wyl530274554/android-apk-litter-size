package com.melon.android;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.melon.android.tool.CommonUtil;
import com.melon.android.tool.Constant;
import com.melon.android.tool.HttpUtil;
import com.melon.android.tool.LogUtil;

import org.json.JSONObject;

import static com.melon.android.tool.ApiUtil.API_APP_UPGRADE;
import static com.melon.android.tool.ApiUtil.APP_DOWNLOAD;

public class MainActivity extends Activity implements TextView.OnEditorActionListener, AdapterView.OnItemClickListener {

    private final String[] mTitles = {"密码"};
    private static final int ITEM_PASSWORD = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editText = findViewById(R.id.et_main_search);
        editText.setOnEditorActionListener(this);

        GridView gridView = findViewById(R.id.gv_main);
        gridView.setAdapter(new MainAdapter());
        gridView.setOnItemClickListener(this);

        getAppUpgradeInfo();
    }

    private void getAppUpgradeInfo() {
        LogUtil.d("getAppUpgradeInfo");

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
            String url = Constant.URL_BAI_DU + content;
            if (content.startsWith("http")) {
                url = content;
            }
            CommonUtil.enterActivity(this, WebActivity.class, "url", url);
            return true;
        }
        return false;
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
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(20);
            tv.setHeight(150);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.CENTER);
            tv.setText(mTitles[i]);
            return tv;
        }
    }
}
