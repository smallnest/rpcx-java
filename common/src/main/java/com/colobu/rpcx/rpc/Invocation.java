package com.colobu.rpcx.rpc;

import java.util.Map;


/**
 * Created by goodjava@qq.com.
 */
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

    void setArguments(Object[] arguments);

    void setParameterTypeNames(String[] parameterTypeNames);


}
