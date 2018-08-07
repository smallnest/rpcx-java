package com.colobu.rpcx.client;

import com.colobu.rpcx.protocol.CompressType;
import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.MessageType;
import com.colobu.rpcx.protocol.SerializeType;
import org.junit.Test;

import java.util.stream.IntStream;

/**
 * Created by zhangzhiyong on 2018/7/3.
 */
public class NettyClientTest {

    /**
     * 直连
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
        Message res = client.call("192.168.31.82:8997", req,1000);
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

        ZkServiceDiscovery serviceDiscovery = new ZkServiceDiscovery("/youpin/services/","Arith");//发现golang的服务
        NettyClient client = new NettyClient(serviceDiscovery);
        Message res = client.call(req,2000);
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
        IntStream.range(0,100).forEach(it->{
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
