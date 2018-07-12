package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.netty.IClient;
import com.colobu.rpcx.protocol.CompressType;
import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.MessageType;
import com.colobu.rpcx.protocol.SerializeType;
import com.colobu.rpcx.rpc.*;
import org.msgpack.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.stream.Stream;

public class RpcInvoker<T> implements Invoker<T> {

    private static final Logger logger = LoggerFactory.getLogger(RpcInvoker.class);


    private IClient client;

    @Override
    public Class<T> getInterface() {
        return null;
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        String className = invocation.getClassName();
        String method = invocation.getMethodName();
        RpcResult result = new RpcResult();

        Message req = new Message(className, method);
        req.setVersion((byte) 0);
        req.setMessageType(MessageType.Request);
        req.setHeartbeat(false);
        req.setOneway(false);
        req.setCompressType(CompressType.None);
        req.setSerializeType(SerializeType.SerializeNone);
        req.setSeq(123);
        try {
            MessagePack messagePack = new MessagePack();
            byte[]data = messagePack.write(invocation);
            req.payload = data;
            Message res = client.call(req);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Class<?> clazz = Class.forName(invocation.getClassName());
            Class[] clazzArray = Stream.of(invocation.getParameterTypeNames()).map(it -> {
                try {
                    return ReflectUtils.name2class(ReflectUtils.desc2name(it));
                } catch (ClassNotFoundException e) {
                    throw new RpcException(e);
                }
            }).toArray(Class[]::new);
            Method m = clazz.getMethod(method, clazzArray);
            Object r = m.invoke(clazz.newInstance(), invocation.getArguments());
            result.setValue(r);
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("class:{} method:{}", className, method);
        return result;
    }
}
