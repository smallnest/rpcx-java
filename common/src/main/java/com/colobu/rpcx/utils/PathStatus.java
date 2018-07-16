package com.colobu.rpcx.utils;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class PathStatus {

    private String type;
    private String value;

    private String path;

    public PathStatus(String type, String value, String path) {
        this.type = type;
        this.value = value;
        this.path = path;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "PathStatus{" +
                "type='" + type + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
