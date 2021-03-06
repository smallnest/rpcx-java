package com.colobu.rpcx.protocol;

/**
 * Created by goodjava@qq.com.
 */
public enum CompressType {
    /**
     * 不压缩
     */
    None(0),
    /**
     * gzip压缩
     */
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
