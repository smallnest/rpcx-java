package com.colobu.rpcx.common;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class TestPoolFactory extends BaseKeyedPooledObjectFactory<String, String> {

    @Override
    public String create(String o) throws Exception {
        return "abc:" + System.currentTimeMillis();
    }

    @Override
    public PooledObject wrap(String v) {
        return new DefaultPooledObject(v);
    }
}
