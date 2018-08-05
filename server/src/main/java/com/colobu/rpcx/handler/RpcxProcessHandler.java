package com.colobu.rpcx.handler;

import com.colobu.rpcx.netty.NettyConnetManageHandler;
import com.colobu.rpcx.netty.NettyDecoder;
import com.colobu.rpcx.netty.NettyEncoder;
import com.colobu.rpcx.netty.NettyServerHandler;
import com.colobu.rpcx.server.NettyServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.List;

public class RpcxProcessHandler extends ByteToMessageDecoder {

    private int serverChannelMaxIdleTimeSeconds;

    private NettyServer nettyServer;

    public RpcxProcessHandler(int serverChannelMaxIdleTimeSeconds, NettyServer nettyServer) {
        this.serverChannelMaxIdleTimeSeconds = serverChannelMaxIdleTimeSeconds;
        this.nettyServer = nettyServer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (in.readableBytes() < 1) {
            return;
        }

        final int magic = in.getByte(in.readerIndex());
        ChannelPipeline p = ctx.pipeline();
        //处理http
        if (isHttp(magic)) {
            p.addLast(new HttpServerCodec());
            p.addLast(new HttpObjectAggregator(1048576));
            p.addLast(new RpcxHttpHandler(nettyServer));
            p.remove(this);
        } else {//处理二进制
            p.addLast(new NettyEncoder());
            p.addLast(new NettyDecoder());
            p.addLast(new IdleStateHandler(0, 0, serverChannelMaxIdleTimeSeconds));
            p.addLast(new NettyConnetManageHandler(nettyServer));
            p.addLast(new NettyServerHandler(nettyServer));
            p.remove(this);
        }

    }


    // G for GET, and P for POST
    private static boolean isHttp(int magic) {
        return magic == 'G' || magic == 'P';
    }
}
