package com.lierda.kesi.ihouse.po;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by qianjiawei on 2017/11/29.
 */

public class Attributes {

    private String USER; //用户ID
    private String TOKEN;//设备TOKEN
    private String METHOD;//

    @JSONField(name = "USER")
    public String getUSER() {
        return USER;
    }

    public void setUSER(String USER) {
        this.USER = USER;
    }

    @JSONField(name = "TOKEN")
    public String getTOKEN() {
        return TOKEN;
    }

    public void setTOKEN(String TOKEN) {
        this.TOKEN = TOKEN;
    }

    @JSONField(name = "METHOD")
    public String getMETHOD() {
        return METHOD;
    }

    public void setMETHOD(String METHOD) {
        this.METHOD = METHOD;
    }

    public Attributes(String USER, String TOKEN, String METHOD) {
        this.USER = USER;
        this.TOKEN = TOKEN;
        this.METHOD = METHOD;
    }

    public Attributes() {
        super();
    }
}
