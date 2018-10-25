package com.colobu.rpcx.handler;

import com.colobu.rpcx.protocol.LanguageCode;
import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.MessageType;
import com.colobu.rpcx.protocol.RemotingCommand;
import com.colobu.rpcx.server.NettyServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.socks.SocksMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * XVersion           = "X-RPCX-Version"
 * XMessageType       = "X-RPCX-MesssageType"
 * XHeartbeat         = "X-RPCX-Heartbeat"
 * XOneway            = "X-RPCX-Oneway"
 * XMessageStatusType = "X-RPCX-MessageStatusType"
 * XSerializeType     = "X-RPCX-SerializeType"
 * XMessageID         = "X-RPCX-MessageID"
 * XServicePath       = "X-RPCX-ServicePath"
 * XServiceMethod     = "X-RPCX-ServiceMethod"
 * XMeta              = "X-RPCX-Meta"
 * XErrorMessage      = "X-RPCX-ErrorMessage"
 *
 * @author goodjava@qq.com
 */
public class RpcxHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(RpcxHttpHandler.class);

    private NettyServer nettyServer;

    public RpcxHttpHandler(NettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        String servicePath = msg.headers().get("X-RPCX-ServicePath");
        String serviceMethod = msg.headers().get("X-RPCX-ServiceMethod");

        logger.info("service:{} method:{}", servicePath, serviceMethod);

        ByteBuf buf = msg.content();
        int len = buf.readableBytes();

        byte[] payload = new byte[len];
        buf.readBytes(payload);

        Message message = new Message();
        message.metadata.put("language", LanguageCode.HTTP.name());
        message.metadata.put("_host", getClientIp(ctx, msg));
        message.metadata.put("_port", "0");
        message.servicePath = servicePath;
        message.serviceMethod = serviceMethod;
        message.payload = payload;
        message.setMessageType(MessageType.Request);
        message.setOneway(true);
        RemotingCommand command = RemotingCommand.createRequestCommand(message);
        command.setCode(1984);
        //这里会异步处理
        nettyServer.processRequestCommand(ctx, command);
    }


    private String getClientIp(ChannelHandlerContext ctx, FullHttpRequest request) {
        HttpHeaders httpHeaders = request.headers();
        String remoteIP = httpHeaders.get("X-Forwarded-For");
        if (remoteIP == null) {
            remoteIP = httpHeaders.get("X-Real-IP");
        }
        if (remoteIP != null) {
            return remoteIP;
        } else { // request取不到就从channel里取
            return ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName();
        }
    }

}
