package com.colobu.rpcx.utils;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class PathStatus {

    private String type;
    private String value;

    public PathStatus(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "PathStatus{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
