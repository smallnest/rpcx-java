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

        IClient client = new NettyClient(null);
        Message res = client.call("10.231.72.75:8976", req);
        System.out.println(new String(res.payload));
    }

    @Test
    public void testSelector() throws Exception {
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

        ServiceDiscovery serviceDiscovery = new ServiceDiscovery("/youpin/services/","Arith");
        IClient client = new NettyClient(serviceDiscovery);
        Message res = client.call(req);
        System.out.println(new String(res.payload));
    }


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

        IClient client = new NettyClient(null);
        IntStream.range(0,100).forEach(it->{
            Message res = null;
            try {
                res = client.call("10.231.72.75:8976", req);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(new String(res.payload));
        });
    }
}
