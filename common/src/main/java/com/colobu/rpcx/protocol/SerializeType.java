package com.colobu.rpcx.protocol;

public enum SerializeType {
    SerializeNone(0),
    JSON(1),
    ProtoBuffer(2),
    MsgPack(3);


    private final int v;

    private SerializeType(int v) {
        this.v = v;
    }

    private static SerializeType[] values = SerializeType.values();
    public static SerializeType getValue(int i) {
        return values[i];
    }

    public int value() {
        return v;
    }
}
