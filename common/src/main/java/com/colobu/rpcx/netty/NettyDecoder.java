package com.colobu.rpcx.netty;

import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.RemotingCommand;
import com.colobu.rpcx.rpc.RpcException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @author goodjava@qq.com
 */
public class NettyDecoder extends ReplayingDecoder<DecoderState> {

    private Message message = null;

    public NettyDecoder() {
        super(DecoderState.MagicNumber);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        switch (state()) {
            case MagicNumber:
                message = new Message();
                byte magicNumber = in.readByte();
                if (magicNumber != Message.magicNumber) {
                    throw new RpcException("magicNumber error:" + magicNumber);
                }
                checkpoint(DecoderState.Header);
            case Header:
                byte[] header = new byte[12];
                header[0] = Message.magicNumber;
                in.readBytes(header, 1, 11);
                message.header = header;
                checkpoint(DecoderState.Body);
            case Body:
                int totalLen = in.readInt();
                byte[] data = new byte[totalLen];
                in.readBytes(data);
                RemotingCommand command = new RemotingCommand(message, data);
                checkpoint(DecoderState.MagicNumber);
                //业务解码交给业务层
                out.add(command);
                break;
            default:
                throw new RpcException("Shouldn't reach here.");
        }
    }


}
