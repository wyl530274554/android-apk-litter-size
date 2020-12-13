package com.melon.android;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.melon.android.fragment.PasswordFragment;
import com.melon.android.tool.LogUtil;


/**
 * 单个fragment页面 可以统一走这里
 * 能减少activity在清单中的注册
 * Created by admin on 2017/3/28.
 * Email 530274554@qq.com
 */

public class CommonFragmentActivity extends Activity {
    public static final int FRAGMENT_PASSWORD = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    protected void initView() {
        setContentView(R.layout.activity_common_fragment);
        FragmentManager fragmentManager = getFragmentManager();
        int target = getIntent().getIntExtra("target", -1);

        if (target < 0) {
            LogUtil.e("目标不存在");
            finish();
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = null;
        switch (target) {
            case FRAGMENT_PASSWORD:
                fragment = new PasswordFragment();
                break;
        }
        transaction.add(R.id.fl_common_fragment, fragment);
        transaction.commit();
    }

}
