package com.colobu.rpcx.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * @author goodjava@qq.com
 */
public class ChannelWrapper {

    private final ChannelFuture channelFuture;


    public ChannelWrapper(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }


    public boolean isOK() {
        return this.channelFuture.channel() != null && this.channelFuture.channel().isActive();
    }


    public boolean isWriteable() {
        return this.channelFuture.channel().isWritable();
    }


    public Channel getChannel() {
        return this.channelFuture.channel();
    }


    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

}
