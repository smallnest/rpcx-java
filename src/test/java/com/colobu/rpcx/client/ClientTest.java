package com.colobu.rpcx.client;

import com.colobu.rpcx.protocol.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

/**
   Test with services in Go.


   <code>
       package main

         import (
         "context"
         "flag"

         "github.com/smallnest/rpcx/server"
         )

         var (
         addr = flag.String("addr", "localhost:8972", "server address")
         )

         type Echo int

         func (t *Echo) Echo(ctx context.Context, args []byte, reply *[]byte) error {
         *reply = []byte("hello" + string(args))
         return nil
         }

         func main() {
         flag.Parse()

         s := server.NewServer()
         s.RegisterName("echo", new(Echo), "")
         s.Serve("tcp", *addr)
         }

   </code>
 */
public class ClientTest {

    @Ignore
    @Test
    public void call() throws Exception {
        //prepare req
        Message req = new Message("echo", "Echo");
        req.setVersion((byte)0);
        req.setMessageType(MessageType.Request);
        req.setHeartbeat(false);
        req.setOneway(false);
        req.setCompressType(CompressType.None);
        req.setSerializeType(SerializeType.SerializeNone);
        req.setSeq(12345678);

        req.metadata.put("test", "1234");

        try {
            req.payload = "world".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            fail("failed to get UTF-8 bytes");
        }


        // create the client
        Client client = new Client();
        client.connect("127.0.0.1", 8972);
        Message res = client.call(req);
        assertNotNull(res);

        assertEquals(req.servicePath, res.servicePath);
        assertEquals(req.serviceMethod,res.serviceMethod);

        assertEquals(req.getVersion(), req.getVersion());
        assertEquals(MessageType.Response, res.getMessageType());
        assertEquals(req.isHeartbeat(), req.isHeartbeat());
        assertEquals(req.isOneway(), req.isOneway());
        assertEquals(req.getCompressType(), req.getCompressType());
        assertEquals(req.getMessageStatusType(), req.getMessageStatusType());
        assertEquals(req.getSeq(), req.getSeq(), 0);

        assertEquals("helloworld", new String(res.payload, "UTF-8"));
    }
}