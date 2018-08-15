package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.fail.FailType;
import com.colobu.rpcx.protocol.LanguageCode;
import com.colobu.rpcx.protocol.SerializeType;
import com.colobu.rpcx.rpc.Invocation;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.URL;
import com.colobu.rpcx.selector.SelectMode;
import io.netty.channel.Channel;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * @author goodjava@qq.com
 */
public class RpcInvocation implements Invocation, Serializable {

    private String className;

    private String methodName;

    private String group = "";

    private Class<?>[] parameterTypes;

    private Class<?> resultType;

    public String[] parameterTypeNames;

    private Object[] arguments;

    private Map<String, String> attachments;

    private transient Invoker<?> invoker;

    public URL url;

    private long timeOut = TimeUnit.SECONDS.toMillis(1);

    private int retryNum = 1;

    private String sendType;

    public LanguageCode languageCode = LanguageCode.JAVA;


    private SerializeType serializeType = SerializeType.SerializeNone;


    private FailType failType = FailType.FailFast;

    private SelectMode selectMode = SelectMode.RandomSelect;

    private transient byte[] payload;

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

    public LanguageCode getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(LanguageCode languageCode) {
        this.languageCode = languageCode;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public SerializeType getSerializeType() {
        return serializeType;
    }

    public void setSerializeType(SerializeType serializeType) {
        this.serializeType = serializeType;
    }
}
