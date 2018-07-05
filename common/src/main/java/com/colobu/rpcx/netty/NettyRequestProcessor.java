
package com.colobu.rpcx.netty;

import com.colobu.rpcx.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;


public interface NettyRequestProcessor {
    RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
            throws Exception;
    boolean rejectRequest();
}
