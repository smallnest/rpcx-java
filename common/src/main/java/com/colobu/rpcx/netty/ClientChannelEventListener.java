package com.colobu.rpcx.netty;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by goodjava@qq.com.
 */
public class ClientChannelEventListener implements ChannelEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ClientChannelEventListener.class);


    public ClientChannelEventListener() {
    }

    @Override
    public void onChannelConnect(String remoteAddr, Channel channel) {
        logger.info("client connect remoteAddr:{}", remoteAddr);
    }

    @Override
    public void onChannelClose(String remoteAddr, Channel channel) {
        logger.info("client close remoteAddr:" + remoteAddr);
    }

    @Override
    public void onChannelException(String remoteAddr, Channel channel) {
        logger.info("client onChannelException  remoteAddr:{}", remoteAddr);
    }

    @Override
    public void onChannelIdle(String remoteAddr, Channel channel) {

    }
}
