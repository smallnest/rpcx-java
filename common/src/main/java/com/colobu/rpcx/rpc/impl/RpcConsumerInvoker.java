package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.discovery.IServiceDiscovery;
import com.colobu.rpcx.netty.IClient;
import com.colobu.rpcx.protocol.*;
import com.colobu.rpcx.rpc.*;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author goodjava@qq.com
 */
public class RpcConsumerInvoker<T> implements Invoker<T> {

    private static final Logger logger = LoggerFactory.getLogger(RpcConsumerInvoker.class);

    private static final AtomicInteger seq = new AtomicInteger();

    private IClient client;

    private URL url;

    public RpcConsumerInvoker(IClient client) {
        this.client = client;
    }

    @Override
    public Class<T> getInterface() {
        return null;
    }

    @Override
    public Result invoke(RpcInvocation invocation) throws RpcException {
        String className = invocation.getClassName();
        String method = invocation.getMethodName();
        RpcResult result = new RpcResult();

        Message req = new Message(className, method);
        req.setVersion((byte) 0);
        req.setMessageType(MessageType.Request);
        req.setHeartbeat(false);
        req.setOneway(false);
        req.setCompressType(CompressType.None);
        req.setSerializeType(invocation.getSerializeType());

        invocation.setUrl(this.url);
        byte[] data = null;
        if (invocation.getLanguageCode().equals(LanguageCode.GO)) {
            req.metadata.put(Constants.LANGUAGE, LanguageCode.GO.name());
            data = invocation.getPayload();
        } else if (invocation.getLanguageCode().equals(LanguageCode.JAVA)) {
            req.metadata.put(Constants.LANGUAGE, LanguageCode.JAVA.name());
            data = HessianUtils.write(invocation);
        }
        req.payload = data;

        try {
            req.setSeq(seq.incrementAndGet());
            Message res = client.call(req, invocation.getTimeOut(), invocation.getSendType());

            if (res.metadata.containsKey(Constants.RPCX_ERROR_CODE)) {
                String code = (res.metadata.get(Constants.RPCX_ERROR_CODE));
                String message = res.metadata.get(Constants.RPCX_ERROR_MESSAGE);
                if (message == null) {
                    message = "";
                }
                logger.warn("client call error:{}:{}", code, message);
                RpcException error = new RpcException(message, code);
                result.setThrowable(error);
            } else {
                byte[] d = res.payload;
                if (d.length > 0) {
                    if (invocation.getLanguageCode().equals(LanguageCode.JAVA)) {
                        Object r = HessianUtils.read(d);
                        result.setValue(r);
                    }
                    if (invocation.getLanguageCode().equals(LanguageCode.GO)) {
                        result.setValue(d);
                    }
                }

            }
        } catch (Throwable e) {
            result.setThrowable(e);
            logger.info("client call error:{} ", e.getMessage());
        }

        logger.info("class:{} method:{} result:{} finish", className, method, new Gson().toJson(result));
        return result;
    }

    @Override
    public void setMethod(Method method) {

    }

    @Override
    public Method getMethod() {
        return null;
    }

    @Override
    public void setInterface(Class clazz) {

    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void destroy() {

    }

    @Override
    public IServiceDiscovery serviceDiscovery() {
        return this.client.getServiceDiscovery();
    }
}
