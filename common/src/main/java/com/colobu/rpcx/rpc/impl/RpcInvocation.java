package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.rpc.Invocation;
import com.colobu.rpcx.rpc.Invoker;

import java.util.Map;

public class RpcInvocation implements Invocation {

    private String className;

    private String methodName;

    private Class<?>[] parameterTypes;

    public String[] parameterTypeNames;

    private Object[] arguments;

    private Map<String, String> attachments;

    private transient Invoker<?> invoker;

    public RpcInvocation() {
    }


    @Override
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    @Override
    public String[] getParameterTypeNames() {
        return this.parameterTypeNames;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    @Override
    public Map<String, String> getAttachments() {
        return attachments;
    }

    @Override
    public String getAttachment(String key) {
        return null;
    }

    @Override
    public String getAttachment(String key, String defaultValue) {
        return null;
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
    }

    @Override
    public Invoker<?> getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker<?> invoker) {
        this.invoker = invoker;
    }

    @Override
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
