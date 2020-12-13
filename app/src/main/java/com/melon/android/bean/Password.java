package com.melon.android.bean;

import java.io.Serializable;

/**
 * Created by melon on 2017/11/30.
 * Email 530274554@qq.com
 */

public class Password implements Serializable {
    public int id;
    public String title;
    public String username;
    public String pwd;
    public String remark;

    public Password(String title, String username, String pwd, String remark) {
        this.title = title;
        this.username = username;
        this.pwd = pwd;
        this.remark = remark;
    }
}
