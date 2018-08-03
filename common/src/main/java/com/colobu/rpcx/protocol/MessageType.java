package com.colobu.rpcx.protocol;

/**
 * Created by goodjava@qq.com.
 */
public enum MessageType {
    /**
     * 请求
     */
    Request(0),

    /**
     * 应答
     */
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
