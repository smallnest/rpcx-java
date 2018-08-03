package com.colobu.rpcx.client;

import com.colobu.rpcx.protocol.Message;

/**
 * Created by goodjava@qq.com.
 */
public interface Callable {
    void call(Message msg);
}
