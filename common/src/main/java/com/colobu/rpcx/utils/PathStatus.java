package com.colobu.rpcx.utils;

import com.colobu.rpcx.common.Pair;

/**
 * @author zhangzhiyong
 * @date 2018/7/4
 */
public class PathStatus {

    private String type;
    private Pair<String,String> value;
    private String path;

    private boolean stop;

    public PathStatus(String type, Pair<String,String> value, String path) {
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

    public Pair<String,String> getValue() {
        return value;
    }

    public void setValue(Pair<String,String> value) {
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
