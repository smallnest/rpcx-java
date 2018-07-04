/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.colobu.rpcx.netty;

import com.colobu.rpcx.common.RemotingHelper;
import com.colobu.rpcx.common.RemotingUtil;
import com.colobu.rpcx.protocol.CompressType;
import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.RemotingCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.GZIPInputStream;


public class NettyDecoder extends ReplayingDecoder{

    private static final Logger log = LoggerFactory.getLogger(NettyDecoder.class);


    public NettyDecoder() {

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        RemotingCommand command = RemotingCommand.createResponseCommand();
        Message message = new Message();
        byte[]header = new byte[12];
        in.readBytes(header);

        ByteBuffer headerBuf = ByteBuffer.wrap(header);
        headerBuf.position(4);
        double seq = headerBuf.getDouble();
        message.setSeq(seq);


        int totalLen  = in.readInt();
        byte[] data = new byte[totalLen];
        in.readBytes(data);

        ByteBuffer buf = ByteBuffer.wrap(data);
        int len = buf.getInt();
        byte[] b = new byte[len];
        buf.get(b);
        message.servicePath = new String(b, "UTF-8");

        len = buf.getInt();
        b = new byte[len];
        buf.get(b);
        message.serviceMethod = new String(b, "UTF-8");

        len = buf.getInt();
        b = new byte[len];
        buf.get(b);
//        decodeMetadata(b);

        len = buf.getInt();
        byte[] payload = new byte[len];
        buf.get(payload);
        message.payload = payload;

        command.setMessage(message);
        out.add(command);
    }

}
