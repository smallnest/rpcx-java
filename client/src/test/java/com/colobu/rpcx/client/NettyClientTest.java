package com.colobu.rpcx.client;

import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.FilterWrapper;
import com.colobu.rpcx.protocol.*;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.URL;
import com.colobu.rpcx.rpc.impl.RpcConsumerInvoker;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import org.junit.Test;

import java.util.stream.IntStream;

/**
 * Created by zhangzhiyong on 2018/7/3.
 */
public class NettyClientTest {


    /**
     * 调用golang 并且使用filter增强
     */
    @Test
    public void testCallGolang() {
        ZkServiceDiscovery serviceDiscovery = new ZkServiceDiscovery("/youpin/services/", "Arith");
        NettyClient client = new NettyClient(serviceDiscovery);
        Invoker invoker = new RpcConsumerInvoker(client);
        Invoker<Object> wrapperInvoker = FilterWrapper.ins().buildInvokerChain(invoker, "", Constants.CONSUMER);

        RpcInvocation invocation = new RpcInvocation();
        invocation.setMethodName("Echo");
        invocation.setClassName("Arith");
        invocation.setLanguageCode(LanguageCode.GO);
        invocation.setPayload("zzy".getBytes());
        invocation.setUrl(new URL("rpcx","",0));
        invocation.setSerializeType(SerializeType.SerializeNone);

        Result result = wrapperInvoker.invoke(invocation);

        if (!result.hasException()){
            System.out.println(new String((byte[])(result.getValue())));
        } else {
            result.getException().printStackTrace();
        }
    }


    /**
     * 直连
     *
     * @throws Exception
     */
    @Test
    public void testSendMsg() throws Exception {
        Message req = new Message("Arith", "Echo");
        req.setVersion((byte) 0);
        req.setMessageType(MessageType.Request);
        req.setHeartbeat(false);
        req.setOneway(false);
        req.setCompressType(CompressType.None);
        req.setSerializeType(SerializeType.SerializeNone);
        req.setSeq(123);
        req.metadata.put("test", "1234");
        req.payload = "world".getBytes("UTF-8");

        NettyClient client = new NettyClient(null);
        Message res = client.call("192.168.31.82:8997", req, 1000);
        System.out.println(new String(res.payload));
    }

    //经过服务发现
    @Test
    public void testGolangServiceFinder() throws Exception {
        Message req = new Message("Arith", "Echo");
        req.setVersion((byte) 0);
        req.setMessageType(MessageType.Request);
        req.setHeartbeat(false);
        req.setOneway(false);
        req.setCompressType(CompressType.None);
        req.setSerializeType(SerializeType.SerializeNone);
        req.setSeq(123);
        req.metadata.put("test", "1234");
        req.payload = "world".getBytes("UTF-8");

        ZkServiceDiscovery serviceDiscovery = new ZkServiceDiscovery("/youpin/services/", "Arith");//发现golang的服务
        NettyClient client = new NettyClient(serviceDiscovery);
        Message res = client.call(req, 2000);
        System.out.println(new String(res.payload));
    }


    //测试循环发送消息
    @Test
    public void testLoopSendMsg() throws Exception {
        Message req = new Message("Arith", "Echo");
        req.setVersion((byte) 0);
        req.setMessageType(MessageType.Request);
        req.setHeartbeat(false);
        req.setOneway(false);
        req.setCompressType(CompressType.None);
        req.setSerializeType(SerializeType.SerializeNone);
        req.setSeq(123);
        req.metadata.put("test", "1234");
        req.payload = "world".getBytes("UTF-8");

        NettyClient client = new NettyClient(null);
        IntStream.range(0, 100).forEach(it -> {
            Message res = null;
            try {
                res = client.call("10.231.72.75:8976", req, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(new String(res.payload));
        });
    }
}
