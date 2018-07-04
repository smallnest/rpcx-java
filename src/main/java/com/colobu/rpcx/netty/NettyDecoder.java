package com.colobu.rpcx.netty;

import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.RemotingCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.nio.ByteBuffer;
import java.util.List;


public class NettyDecoder extends ReplayingDecoder{

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
