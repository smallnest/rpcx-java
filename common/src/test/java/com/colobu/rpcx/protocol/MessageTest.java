package com.colobu.rpcx.protocol;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;


public class MessageTest {


    @Test
    public void encodeAndDecode() {
        Message req = new Message("arith", "Mul");

        req.setVersion((byte)1);
        req.setMessageType(MessageType.Request);
        req.setHeartbeat(true);
        req.setOneway(true);
        req.setCompressType(CompressType.None);
        req.setMessageStatusType(MessageStatusType.Normal);
        req.setSerializeType(SerializeType.JSON);
        req.setSeq(12345678);

        req.metadata.put("auth", "false");
        req.metadata.put("test", "1234");
        try {
            req.payload = "abcdefg".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            fail("failed to get UTF-8 bytes");
        }
        try {
            byte[] data = req.encode();
            Message res = new Message();
            res.decode(new ByteArrayInputStream(data));

            assertEquals(req.servicePath, res.servicePath);
            assertEquals(req.serviceMethod,res.serviceMethod);
            assertArrayEquals(req.payload, res.payload);
            assertEquals(2, res.metadata.size());
            assertEquals("false", res.metadata.get("auth"));
            assertEquals("1234", res.metadata.get("test"));

            assertEquals(req.getVersion(), req.getVersion());
            assertEquals(req.getMessageType(), req.getMessageType());
            assertEquals(req.isHeartbeat(), req.isHeartbeat());
            assertEquals(req.isOneway(), req.isOneway());
            assertEquals(req.getCompressType(), req.getCompressType());
            assertEquals(req.getMessageStatusType(), req.getMessageStatusType());
            assertEquals(req.getSeq(), req.getSeq(), 0);



        } catch (Exception e) {
            e.printStackTrace();
            fail("failed to encode/decode: " + e.getMessage());
        }

    }
}