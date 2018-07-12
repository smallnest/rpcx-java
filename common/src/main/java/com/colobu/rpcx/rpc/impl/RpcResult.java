package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.rpc.Result;

import java.util.Map;

public class RpcResult implements Result {


    private Object value;

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Throwable getException() {
        return null;
    }

    @Override
    public boolean hasException() {
        return false;
    }

    @Override
    public Map<String, String> getAttachments() {
        return null;
    }

    @Override
    public String getAttachment(String key) {
        return null;
    }

    @Override
    public String getAttachment(String key, String defaultValue) {
        return null;
    }
}
