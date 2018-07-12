package com.colobu.rpcx.rpc;

import java.util.Map;

public interface Invocation {

    String getMethodName();

    String getClassName();

    Class<?>[] getParameterTypes();

    String[] getParameterTypeNames();

    Object[] getArguments();

    Map<String, String> getAttachments();

    String getAttachment(String key);

    String getAttachment(String key, String defaultValue);

    Invoker<?> getInvoker();

    Class<?> getResultType();


}
