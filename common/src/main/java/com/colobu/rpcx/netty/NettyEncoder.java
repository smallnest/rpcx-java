package com.colobu.rpcx.netty;

import com.colobu.rpcx.common.RemotingHelper;
import com.colobu.rpcx.common.RemotingUtil;
import com.colobu.rpcx.protocol.RemotingCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * @author goodjava@qq.com
 */
public class NettyEncoder extends MessageToByteEncoder<RemotingCommand> {

    private static final Logger log = LoggerFactory.getLogger(NettyEncoder.class);

    @Override
    public void encode(ChannelHandlerContext ctx, RemotingCommand remotingCommand, ByteBuf out) {
        try {
            byte[] data = remotingCommand.getMessage().encode();
            out.writeBytes(data);
        } catch (Exception e) {
            log.error("encode exception, " + RemotingHelper.parseChannelRemoteAddr(ctx.channel()), e);
            if (remotingCommand != null) {
                log.error(remotingCommand.toString());
            }
            RemotingUtil.closeChannel(ctx.channel());
        }
    }
}
