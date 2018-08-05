package com.colobu.rpcx.handler;

import com.colobu.rpcx.processor.RpcProcessor;
import com.colobu.rpcx.protocol.LanguageCode;
import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.RemotingCommand;
import com.colobu.rpcx.server.NettyServer;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

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
 */
public class RpcxHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(RpcxHttpHandler.class);

    private NettyServer nettyServer;

    public RpcxHttpHandler(NettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        logger.info(msg.getUri());

        msg.headers().forEach(it -> {
            logger.info("--------->{}:{}", it.getKey(), it.getValue());
        });

        String servicePath = msg.headers().get("X-RPCX-ServicePath");
        String serviceMethod = msg.headers().get("X-RPCX-ServiceMethod");

        ByteBuf buf = msg.content();
        int len = buf.readableBytes();

        byte[] payload = new byte[len];
        buf.readBytes(payload);

        RemotingCommand command = RemotingCommand.createRequestCommand(-1);
        Message message = new Message();
        message.metadata.put("language", LanguageCode.HTTP.name());
        message.metadata.put("_host", getClientIp(ctx, msg));
        message.metadata.put("_port", "0");
        message.servicePath = servicePath;
        message.serviceMethod = serviceMethod;
        message.payload = payload;
        command.setMessage(message);


        RpcProcessor processor = new RpcProcessor(nettyServer.getGetBeanFunc());

        RemotingCommand res = processor.processRequest(ctx, command);

        logger.info("message:{}", res.getMessage());


        byte[] data = new Gson().toJson(res.getMessage()).getBytes();


        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                OK, Unpooled.wrappedBuffer(data));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH,
                response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, "close");

        ctx.write(response);
        ctx.flush();

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
