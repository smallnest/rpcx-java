package com.colobu.rpcx.netty;

/**
 * Created by goodjava@qq.com.
 */
public enum NettyEventType {
    /**
     * 连接
     */
    CONNECT,

    /**
     * 关闭
     */
    CLOSE,

    /**
     * 空闲
     */
    IDLE,

    /**
     * 异常
     */
    EXCEPTION
}
