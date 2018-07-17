package com.colobu.rpcx.netty;

import com.colobu.rpcx.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by zhangzhiyong on 2018/7/4.
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RemotingCommand> {

    private final NettyRemotingAbstract nettyServer;

    public NettyServerHandler(NettyRemotingAbstract nettyClient) {
        this.nettyServer = nettyClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) {
        nettyServer.processMessageReceived(ctx, msg);
    }
}
