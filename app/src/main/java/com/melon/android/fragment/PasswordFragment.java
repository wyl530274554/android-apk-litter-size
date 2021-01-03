package com.melon.android.fragment;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.melon.android.R;
import com.melon.android.bean.Password;
import com.melon.android.tool.ApiUtil;
import com.melon.android.tool.CommonUtil;
import com.melon.android.tool.HttpUtil;
import com.melon.android.tool.LogUtil;
import com.melon.android.tool.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 笔记主页
 */
@TargetApi(Build.VERSION_CODES.M)
public class PasswordFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener, TextView.OnEditorActionListener {
    public ListView lv_password;
    private MyAdapter mAdapter;
    private List<Password> mPasswords = new ArrayList<>();
    private List<Password> mPasswordsBackup = new ArrayList<>();
    public TextView emptyView;
    public EditText et_pwd_search;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password, container, false);
        init(view);
        initEmptyView();
        return view;
    }

    protected void init(View view) {
        lv_password = view.findViewById(R.id.lv_password);
        view.findViewById(R.id.bt_password_add).setOnClickListener(this);
        et_pwd_search = view.findViewById(R.id.et_pwd_search);
        emptyView = view.findViewById(R.id.empty);
        mAdapter = new MyAdapter();
        lv_password.setAdapter(mAdapter);
        lv_password.setOnItemClickListener(this);
        lv_password.setOnItemLongClickListener(this);

        et_pwd_search.setOnEditorActionListener(this);
    }

    public void initDataShow(List<Password> pwds) {
        mPasswords.clear();
        mPasswordsBackup.clear();

        mPasswords.addAll(pwds);
        mPasswordsBackup.addAll(pwds);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initEmptyView() {
        lv_password.setEmptyView(emptyView);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_password_add:
                //添加笔记
                addPassword();
                break;
            default:
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void addPassword() {
        final LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        final EditText etTitle = new EditText(getContext());
        etTitle.setHint("输入标题");
        final EditText etUser = new EditText(getContext());
        etUser.setHint("输入账号");
        final EditText etPwd = new EditText(getContext());
        etPwd.setHint("输入密码");
        final EditText etDesc = new EditText(getContext());
        etDesc.setHint("输入其它描述");

        container.addView(etTitle);
        container.addView(etUser);
        container.addView(etPwd);
        container.addView(etDesc);

        final AlertDialog mDialog = new AlertDialog.Builder(getActivity()).setView(container).setPositiveButton("确定", null).setNegativeButton("取消", null).create();
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveBtn = mDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = mDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                positiveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String title = etTitle.getText().toString().trim();
                        String user = etUser.getText().toString().trim();
                        String pwd = etPwd.getText().toString().trim();
                        String desc = etDesc.getText().toString().trim();
                        if (CommonUtil.isEmpty(title)) {
                            return;
                        }

                        //上传至服务器
                        uploadPassword(new Password(title, user, pwd, desc), mDialog);
                    }
                });
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });
            }

        });
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        CommonUtil.hideInputMode(getActivity(), true);
                    }
                });
            }
        });
        mDialog.show();
    }

    private void uploadPassword(final Password password, final AlertDialog mDialog) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", password.title);
        map.put("pwd", password.pwd);
        map.put("username", password.username);
        map.put("remark", password.remark);

        HttpUtil.doPost(getContext(), ApiUtil.API_PASSWORD, map, new HttpUtil.HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                if ("1".equalsIgnoreCase(response)) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("添加成功");
                            mDialog.dismiss();
                        }
                    });
                } else {
                    showToast("添加失败: " + response);
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                showToast("uploadPassword error: " + e.getMessage());
            }
        });
    }

    private void showToast(String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.toast(getContext(), msg);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.show(position);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // 修改密码
        Password password = mPasswords.get(position);
        updatePassword(password);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void updatePassword(final Password password) {
        //显示修改对话框
        final LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        final EditText etTitle = new EditText(getContext());
        etTitle.setText(password.title);
        final EditText etUser = new EditText(getContext());
        etUser.setText(password.username);
        final EditText etPwd = new EditText(getContext());
        etPwd.setText(password.pwd);
        final EditText etDesc = new EditText(getContext());
        etDesc.setText(password.remark);

        container.addView(etTitle);
        container.addView(etUser);
        container.addView(etPwd);
        container.addView(etDesc);
        final AlertDialog mDialog = new AlertDialog.Builder(getActivity()).setView(container).setPositiveButton("确定", null).setNegativeButton("取消", null).create();
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveBtn = mDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = mDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                positiveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        password.title = etTitle.getText().toString().trim();
                        password.username = etUser.getText().toString().trim();
                        password.pwd = etPwd.getText().toString().trim();
                        password.remark = etDesc.getText().toString().trim();
                        if (CommonUtil.isEmpty(password.title)) {
                            ToastUtil.toast(getContext(), "内容不能为空");
                            return;
                        }
                        //上传至服务器
                        uploadUpdatedPassword(password, mDialog);
                    }
                });
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });
            }

        });
        mDialog.show();
    }

    /**
     * 更新密码
     */
    private void uploadUpdatedPassword(final Password password, final AlertDialog mDialog) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", password.id);
        map.put("title", password.title);
        map.put("pwd", password.pwd);
        map.put("username", password.username);
        map.put("remark", password.remark);

        HttpUtil.doPost(getContext(), ApiUtil.API_PASSWORD+"update", map, new HttpUtil.HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                if ("1".equalsIgnoreCase(response)) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("更新成功");
                            mDialog.dismiss();
                        }
                    });
                } else {
                    showToast("更新失败: " + response);
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                showToast("uploadPassword error: " + e.getMessage());
            }
        });
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        LogUtil.d("onEditorAction:" + actionId);
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            //点击搜索要做的操作
            String trim = textView.getText().toString().trim();
            getPassword(trim);
            LogUtil.d("getServerNotes:" + trim);
        }
        return false;
    }

    class MyAdapter extends BaseAdapter {
        private int showPos;

        @Override
        public int getCount() {
            return mPasswords == null ? 0 : mPasswords.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_password, parent, false);
            }

            TextView tv_password_title = convertView.findViewById(R.id.tv_password_title);
            TextView tv_password_user = convertView.findViewById(R.id.tv_password_user);
            TextView tv_password_pwd = convertView.findViewById(R.id.tv_password_pwd);
            TextView tv_password_other = convertView.findViewById(R.id.tv_password_other);

            //收起/展开
            if (showPos == position) {
                tv_password_user.setVisibility(View.VISIBLE);
                tv_password_pwd.setVisibility(View.VISIBLE);
                tv_password_other.setVisibility(View.VISIBLE);
            } else {
                tv_password_user.setVisibility(View.GONE);
                tv_password_pwd.setVisibility(View.GONE);
                tv_password_other.setVisibility(View.GONE);
            }

            Password password = mPasswords.get(position);
            tv_password_title.setText("(" + (++position) + ") " + password.title);

            if (CommonUtil.isEmpty(password.username)) {
                tv_password_user.setVisibility(View.GONE);
                tv_password_user.setText("");
            } else {
                tv_password_user.setText("账号：" + password.username);
            }

            if (CommonUtil.isEmpty(password.pwd)) {
                tv_password_pwd.setText("");
                tv_password_pwd.setVisibility(View.GONE);
            } else {
                tv_password_pwd.setText("密码：" + password.pwd);
            }

            if (CommonUtil.isEmpty(password.remark)) {
                tv_password_other.setText("");
                tv_password_other.setVisibility(View.GONE);
            } else {
                tv_password_other.setText(password.remark);
            }

            return convertView;
        }

        public void show(int position) {
            showPos = position;
            notifyDataSetChanged();
        }

    }

    private void getPassword(String content) {
        HttpUtil.doGet(getContext(), ApiUtil.API_PASSWORD + content, new HttpUtil.HttpCallbackStringListener() {
            @Override
            public void onFinish(String response) {
                try {
                    List<Password> passwords = new ArrayList<>();
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.optJSONObject(i);
                        int id = jsonObject.optInt("id");
                        String title = jsonObject.optString("title");
                        String username = jsonObject.optString("username");
                        String pwd = jsonObject.optString("pwd");
                        String remark = jsonObject.optString("remark");
                        Password password = new Password(title, username, pwd, remark);
                        password.id = id;
                        passwords.add(password);
                    }
                    if (passwords.size() > 0) {
                        // 获取本地并显示
                        initDataShow(passwords);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception e) {
                showToast("get notes error: " + e.getMessage());
            }
        });
    }
}
