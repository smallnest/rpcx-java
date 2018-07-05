package com.colobu.rpcx.protocol;

public enum MessageStatusType {
    Normal(0),
    Error(1);


    private final int v;

    private MessageStatusType(int v) {
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
