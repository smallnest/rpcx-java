package com.colobu.rpcx.netty;

/**
 * Created by goodjava@qq.com.
 */
public enum DecoderState {
    MagicNumber,
    Header,
    Body,
}
