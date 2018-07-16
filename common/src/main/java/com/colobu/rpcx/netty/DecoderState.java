package com.colobu.rpcx.netty;

/**
 * Created by zhangzhiyong on 2018/7/5.
 */
public enum DecoderState {
    MagicNumber,
    Header,
    Body,
}
