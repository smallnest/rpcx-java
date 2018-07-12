package com.colobu.rpcx.rpc;

import java.util.Map;

public interface Result {

    Object getValue();

    Throwable getException();

    boolean hasException();

    Map<String, String> getAttachments();

    String getAttachment(String key);

    String getAttachment(String key, String defaultValue);

}
