package com.colobu.rpcx.protocol;

public enum CompressType {
    None(0),
    Gzip(1);


    private final int v;

    CompressType(int v) {
        this.v = v;
    }

    private static CompressType[] values = CompressType.values();
    public static CompressType getValue(int i) {
        return values[i];
    }

    public int value() {
        return v;
    }
}
