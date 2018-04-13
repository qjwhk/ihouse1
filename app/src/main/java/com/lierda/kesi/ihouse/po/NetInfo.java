package com.lierda.kesi.ihouse.po;

/**
 * Created by qianjiawei on 2018/4/4.
 */

public class NetInfo {
    private String status;//0  未連接  1連接
    private String type="-1";//-1未連接  0 數據網  1無線網

    public NetInfo() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
