package com.lierda.kesi.ihouse.po;

/**
 * Created by qianjiawei on 2017/11/29.
 */

public class Alarm {
    private String sourceId;//服务器地址
    private String requestType;
    private String id; //设备MAC
    private Attributes attributes;

    public Alarm(String sourceId, String requestType, String id, Attributes attributes) {
        this.sourceId = sourceId;
        this.requestType = requestType;
        this.id = id;
        this.attributes = attributes;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public Alarm() {
        super();
    }
}
