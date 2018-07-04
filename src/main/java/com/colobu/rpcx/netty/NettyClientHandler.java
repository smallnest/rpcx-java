package com.colobu.rpcx.netty;

import com.colobu.rpcx.client.NettyClient;
import com.colobu.rpcx.common.Pair;
import com.colobu.rpcx.common.RemotingHelper;
import com.colobu.rpcx.protocol.RemotingCommand;
import com.colobu.rpcx.protocol.RemotingSysResponseCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by zhangzhiyong on 2018/7/3.
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RemotingCommand> {

    private final NettyRemotingAbstract nettyClient;

    public NettyClientHandler(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {
        nettyClient.processMessageReceived(ctx, msg);
    }


}
