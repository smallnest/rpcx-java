package com.colobu.rpcx.utils;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class PathStatus {

    private String type;
    private String value;

    private String path;

    private boolean stop;

    public PathStatus(String type, String value, String path) {
        this.type = type;
        this.value = value;
        this.path = path;
    }

    public PathStatus(boolean stop) {
        this.stop = stop;
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

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
