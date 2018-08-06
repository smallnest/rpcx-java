package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.fail.FailType;
import com.colobu.rpcx.protocol.LanguageCode;
import com.colobu.rpcx.rpc.Invocation;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.URL;
import com.colobu.rpcx.selector.SelectMode;
import io.netty.channel.Channel;

import java.io.Serializable;
import java.util.Map;


/**
 * @author goodjava@qq.com
 */
public class RpcInvocation implements Invocation, Serializable {

    private String className;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Class<?> resultType;

    public String[] parameterTypeNames;

    private Object[] arguments;

    private Map<String, String> attachments;

    private transient Invoker<?> invoker;

    public String servicePath;

    public String serviceMethod;

    public URL url;

    //超时时间
    private long timeOut;

    //重试次数
    private int retryNum = 1;

    private String sendType;

    public LanguageCode languageCode;

    private FailType failType;

    private SelectMode selectMode;

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

    @Override
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

    @Override
    public Class<?> getResultType() {
        return resultType;
    }

    public void setResultType(Class<?> resultType) {
        this.resultType = resultType;
    }

    @Override
    public void setParameterTypeNames(String[] parameterTypeNames) {
        this.parameterTypeNames = parameterTypeNames;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public int getRetryNum() {
        return retryNum;
    }

    public void setRetryNum(int retryNum) {
        this.retryNum = retryNum;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getSendType() {
        return sendType;
    }

    public void setSendType(String sendType) {
        this.sendType = sendType;
    }

    public FailType getFailType() {
        return failType;
    }

    public void setFailType(FailType failType) {
        this.failType = failType;
    }


    public SelectMode getSelectMode() {
        return selectMode;
    }

    public void setSelectMode(SelectMode selectMode) {
        this.selectMode = selectMode;
    }
}
