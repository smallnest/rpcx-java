package com.colobu.rpcx.rpc.impl;

import com.colobu.rpcx.netty.IClient;
import com.colobu.rpcx.protocol.CompressType;
import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.MessageType;
import com.colobu.rpcx.protocol.SerializeType;
import com.colobu.rpcx.rpc.HessianUtils;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class RpcInvoker<T> implements Invoker<T> {

    private static final Logger logger = LoggerFactory.getLogger(RpcInvoker.class);

    private final AtomicInteger seq = new AtomicInteger();


    private IClient client;

    public RpcInvoker(IClient client) {
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
        req.setSerializeType(SerializeType.SerializeNone);
        req.setSeq(seq.incrementAndGet());
        req.metadata.put("language", "java");
        try {
            byte[] data = HessianUtils.write(invocation);
            req.payload = data;
            Message res = client.call(req,invocation.getTimeOut());
            byte[] d = res.payload;
            if (d.length > 0) {
                Object r = HessianUtils.read(d);
                result.setValue(r);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            result.setThrowable(e);
        }

        logger.info("class:{} method:{}", className, method);
        return result;
    }
}
