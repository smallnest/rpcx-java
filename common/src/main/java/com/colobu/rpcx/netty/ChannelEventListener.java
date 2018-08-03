package com.colobu.rpcx.netty;

import io.netty.channel.Channel;

/**
 * Created by goodjava@qq.com.
 */
public interface ChannelEventListener {

    void onChannelConnect(final String remoteAddr, final Channel channel);

    void onChannelClose(final String remoteAddr, final Channel channel);

    void onChannelException(final String remoteAddr, final Channel channel);

    void onChannelIdle(final String remoteAddr, final Channel channel);
}
