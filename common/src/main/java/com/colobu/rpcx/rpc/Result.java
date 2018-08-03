package com.colobu.rpcx.rpc;

import java.util.Map;


/**
 * Created by goodjava@qq.com.
 */
public interface Result {

    void setValue(Object value);

    Object getValue();

    Throwable getException();

    boolean hasException();

    Map<String, String> getAttachments();

    String getAttachment(String key);

    String getAttachment(String key, String defaultValue);

    void setThrowable(Throwable throwable);

}
