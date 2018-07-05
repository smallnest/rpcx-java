package com.colobu.rpcx.client;

import com.colobu.rpcx.protocol.Message;

public interface Callable {
    void call(Message msg);
}
