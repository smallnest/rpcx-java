package com.colobu.rpcx.protocol;

/**
 * Created by goodjava@qq.com.
 */
public enum SerializeType {
    SerializeNone(0),
    JSON(1),
    ProtoBuffer(2),
    MsgPack(3);


    private final int v;

    SerializeType(int v) {
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
