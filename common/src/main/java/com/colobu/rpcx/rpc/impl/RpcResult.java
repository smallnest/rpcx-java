package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.rpc.Result;

import java.util.Map;

public class RpcResult implements Result {

    private Object value;

    private Throwable throwable;

    public RpcResult(Object value) {
        this.value = value;
    }

    public RpcResult() {
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Throwable getException() {
        return this.throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public boolean hasException() {
        return null != throwable;
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
