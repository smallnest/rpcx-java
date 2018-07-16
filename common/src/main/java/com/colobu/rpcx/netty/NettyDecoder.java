package com.colobu.rpcx.netty;

import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.MessageType;
import com.colobu.rpcx.protocol.RemotingCommand;
import com.colobu.rpcx.rpc.RpcException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;


public class NettyDecoder extends ReplayingDecoder<DecoderState> {

    private Message message = new Message();


    public NettyDecoder() {
        super(DecoderState.MagicNumber);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case MagicNumber:
                byte magicNumber = in.readByte();
                if (magicNumber != Message.magicNumber) {
                    throw new RpcException("magicNumber error:"+magicNumber);
                }
                checkpoint(DecoderState.Header);
            case Header:
                byte[] header = new byte[12];
                header[0] = Message.magicNumber;
                in.readBytes(header,1,11);
                message.setMessageType(header[2]);//消息类型
                ByteBuffer headerBuf = ByteBuffer.wrap(header);
                headerBuf.position(4);
                long seq = headerBuf.getLong();
                message.setSeq(seq);//消息的序列号
                checkpoint(DecoderState.Body);
            case Body:
                int totalLen = in.readInt();
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
                decodeMetadata(b, message);

                len = buf.getInt();
                byte[] payload = new byte[len];
                buf.get(payload);
                message.payload = payload;
                MessageType type = message.getMessageType();
                RemotingCommand command = null;
                if (type == MessageType.Request) {
                    command = RemotingCommand.createRequestCommand(2);//request
                } else {
                    command = RemotingCommand.createResponseCommand();//response
                }
                command.setMessage(message);
                checkpoint(DecoderState.MagicNumber);
                out.add(command);
                break;
            default:
                throw new Error("Shouldn't reach here.");
        }
    }


    private void decodeMetadata(byte[] b, Message message) throws UnsupportedEncodingException {
        ByteBuffer buf = ByteBuffer.wrap(b);
        int len;
        for (; ; ) {
            if (buf.remaining() < 4) {
                break;
            }
            len = buf.getInt();
            b = new byte[len];
            buf.get(b);
            String k = new String(b, "UTF-8");

            len = buf.getInt();
            b = new byte[len];
            buf.get(b);
            String v = new String(b, "UTF-8");
            message.metadata.put(k, v);
        }
    }

}
