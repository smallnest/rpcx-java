package com.colobu.rpcx.protocol;

/**
 * Created by goodjava@qq.com.
 */
public enum SerializeType {

    /**
     * java序列化
     */
    SerializeNone(0),
    /**
     * json序列化
     */
    JSON(1),
    /**
     * protobuf序列化
     */
    ProtoBuffer(2),
    /**
     * msgpack序列化
     */
    MsgPack(3),


    Thrift(4);


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
