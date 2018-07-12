package com.colobu.rpcx.rpc;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectUtilsTest {


    @Test
    public void testForName() throws ClassNotFoundException {
        int i = 12;
        System.out.println(ReflectUtils.getDesc(int.class));
        System.out.println(ReflectUtils.getDesc(A.class));


        String str = ReflectUtils.getDesc(int.class);
        System.out.println(ReflectUtils.name2class(ReflectUtils.desc2name(str)));
    }


    @Test
    public void testMethod() throws NoSuchMethodException {
        Method m = IA.class.getMethod("sum", int.class, int.class);
        System.out.println(m);

        Class<?>[] types = m.getParameterTypes();
        System.out.println(Arrays.toString(types));

        Class<?> resType = m.getReturnType();
        System.out.println(resType);
    }
}
