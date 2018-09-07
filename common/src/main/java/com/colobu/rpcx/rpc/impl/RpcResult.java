package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.rpc.Result;
import java.util.HashMap;
import java.util.Map;

/**
 * @author goodjava@qq.com
 */
public class RpcResult implements Result {

    private Object value;

    private Throwable throwable;

    private Map<String, String> attachments = new HashMap<>();

    public RpcResult(Object value) {
        this.value = value;
    }

    public RpcResult() {
    }


    public RpcResult(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
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

    @Override
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public boolean hasException() {
        return null != throwable;
    }

    @Override
    public Map<String, String> getAttachments() {
        return attachments;
    }

    @Override
    public String getAttachment(String key) {
        return this.attachments.get(key);
    }

    @Override
    public String getAttachment(String key, String defaultValue) {
        String result = attachments.get(key);
        if (result == null || result.length() == 0) {
            result = defaultValue;
        }
        return result;
    }


}
