package com.colobu.rpcx.netty;

/**
 * @author goodjava@qq.com
 */
public enum DecoderState {
    /**
     * 魔术值
     */
    MagicNumber,
    /**
     * 包头
     */
    Header,
    /**
     * 包体
     */
    Body,
}
