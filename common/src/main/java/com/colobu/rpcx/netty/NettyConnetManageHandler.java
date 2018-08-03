package com.colobu.rpcx.netty;

import com.colobu.rpcx.common.RemotingHelper;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

import static com.colobu.rpcx.common.RemotingUtil.closeChannel;

/**
 * Created by goodjava@qq.com.
 */
public class NettyConnetManageHandler extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory.getLogger(NettyConnetManageHandler.class);

    private final NettyRemotingAbstract nettyRemotingAbstract;

    public NettyConnetManageHandler(NettyRemotingAbstract nettyRemotingAbstract) {
        this.nettyRemotingAbstract = nettyRemotingAbstract;
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
            throws Exception {
        final String local = localAddress == null ? "UNKNOW" : localAddress.toString();
        final String remote = remoteAddress == null ? "UNKNOW" : remoteAddress.toString();
        log.info("NETTY CLIENT PIPELINE: CONNECT  {} => {}", local, remote);
        super.connect(ctx, remoteAddress, localAddress, promise);

        if (nettyRemotingAbstract.hasEventListener()) {
            nettyRemotingAbstract.putNettyEvent(new NettyEvent(NettyEventType.CONNECT, remoteAddress.toString(), ctx.channel()));
        }
    }


    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.info("NETTY CLIENT PIPELINE: DISCONNECT {}", remoteAddress);
        closeChannel(ctx.channel());
        super.disconnect(ctx, promise);

        if (nettyRemotingAbstract.hasEventListener()) {
            nettyRemotingAbstract.putNettyEvent(new NettyEvent(NettyEventType.CLOSE, remoteAddress.toString(), ctx.channel()));
        }
    }


    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.info("NETTY CLIENT PIPELINE: CLOSE {}", remoteAddress);
        nettyRemotingAbstract.closeChannel(ctx.channel());
        super.close(ctx, promise);

        if (nettyRemotingAbstract.hasEventListener()) {
            nettyRemotingAbstract.putNettyEvent(new NettyEvent(NettyEventType.CLOSE, remoteAddress.toString(), ctx.channel()));
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent evnet = (IdleStateEvent) evt;
            if (evnet.state().equals(IdleState.ALL_IDLE)) {
                final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
                log.warn("NETTY CLIENT PIPELINE: IDLE exception [{}]", remoteAddress);
                closeChannel(ctx.channel());
                if (nettyRemotingAbstract.hasEventListener()) {
                    nettyRemotingAbstract
                            .putNettyEvent(new NettyEvent(NettyEventType.IDLE, remoteAddress.toString(), ctx.channel()));
                }
            }
        }

        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
        log.warn("NETTY CLIENT PIPELINE: exceptionCaught {}", remoteAddress);
        log.warn("NETTY CLIENT PIPELINE: exceptionCaught exception.", cause);
        closeChannel(ctx.channel());
        if (nettyRemotingAbstract.hasEventListener()) {
            nettyRemotingAbstract.putNettyEvent(new NettyEvent(NettyEventType.EXCEPTION, remoteAddress.toString(), ctx.channel()));
        }
    }

}
