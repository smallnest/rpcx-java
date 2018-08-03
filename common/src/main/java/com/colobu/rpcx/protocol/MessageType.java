package com.colobu.rpcx.protocol;

/**
 * Created by goodjava@qq.com.
 */
public enum MessageType {
    Request(0),
    Response(1);


    private final int value;

    MessageType(int value) {
        this.value = value;
    }

    private static MessageType[] values = MessageType.values();
    public static MessageType getValue(int i) {
        return values[i];
    }
}
