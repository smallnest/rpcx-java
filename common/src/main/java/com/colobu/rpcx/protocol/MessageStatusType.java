package com.colobu.rpcx.protocol;

/**
 * Created by goodjava@qq.com.
 */
public enum MessageStatusType {
    /**
     * 一般
     */
    Normal(0),
    /**
     * 错误
     */
    Error(1);


    private final int v;

    MessageStatusType(int v) {
        this.v = v;
    }

    private static MessageStatusType[] values = MessageStatusType.values();
    public static MessageStatusType getValue(int i) {
        return values[i];
    }

    public int value() {
        return v;
    }
}
