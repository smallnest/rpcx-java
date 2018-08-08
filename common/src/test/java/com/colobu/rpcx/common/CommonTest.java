package com.colobu.rpcx.common;

import com.colobu.rpcx.common.retry.RetryNTimes;
import com.colobu.rpcx.common.retry.RetryPolicy;
import com.colobu.rpcx.protocol.RemotingCommand;
import com.colobu.rpcx.rpc.A;
import com.colobu.rpcx.rpc.HessianUtils;
import com.colobu.rpcx.rpc.ReflectUtils;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by goodjava@qq.com.
 */
public class CommonTest {


    class Bean {
        public String str(String s) {
            return "str:" + s;
        }
    }


    @Test
    public void testMethdHandle() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType mt = MethodType.methodType(String.class, String.class);
        MethodHandle mh = lookup.findVirtual(Bean.class, "str", mt);
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            String s = (String) mh.invokeExact(new Bean(), "zzy");
//            System.out.println(s);
        }
        System.out.println(System.currentTimeMillis() - begin);
    }

    @Test
    public void testReflect() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Bean bean = new Bean();
        Method method = bean.getClass().getMethod("str", String.class);
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            String res = (String) method.invoke(bean, "zzy");
//            System.out.println(res);
        }
        System.out.println(System.currentTimeMillis() - begin);
    }


//    @Test
//    public void testReflectAsm() {
//        Bean bean = new Bean();
//        MethodAccess access = MethodAccess.get(Bean.class);
//        long begin = System.currentTimeMillis();
//        for (int i = 0; i < 100000000; i++) {
//            String res = (String) access.invoke(bean, "str", "zzy");
////            System.out.println(res);
//        }
//        System.out.println(System.currentTimeMillis() - begin);
//    }


    @Test
    public void testClassName() {
        System.out.println(ReflectUtils.getName(byte[].class));
    }


    @Test
    public void testCode() {
        RemotingCommand c = RemotingCommand.createResponseCommand();
        c.markOnewayRPC();
        System.out.println(c.isOnewayRPC());
    }


    @Test
    public void testClassUtils() throws ClassNotFoundException {

        String desc = ReflectUtils.getDesc(int.class);
        System.out.println(desc);

        System.out.println(ReflectUtils.desc2name(desc));


        System.out.println(ReflectUtils.getName(int.class));

        System.out.println(ReflectUtils.name2class("int"));

//        System.out.println(ClassUtils.getClassByName("int.class"));
    }


    @Test
    public void getDesc() {
        String desc = ReflectUtils.getDesc(new byte[]{}.getClass());
        System.out.println(desc);
    }


    @Test
    public void testRetry() {
        RetryPolicy retryPolicy = new RetryNTimes(10);
        retryPolicy.retry((i) -> {
            System.out.println(i);
            if (i == 3) {
            }
            return null;
        });
    }


    @Test
    public void testConfig() {
        System.out.println(Config.ins().get("server.type"));
    }


    @Test
    public void testClassPathResource() {
        System.out.println(new ClassPathResource("test").getString());
        System.out.println(new ClassPathResource("test").getProperties().getProperty("2"));
    }


    class E {
        public E() {
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                System.out.println("schedule");
            }, 10, 1, TimeUnit.SECONDS);

            Executors.newSingleThreadScheduledExecutor()
                    .scheduleWithFixedDelay(() -> {
                        System.out.println("schedule");
                    }, 0, 5, TimeUnit.SECONDS);
        }
    }


    @Test
    public void testSchedule() {
        E e = new E();
        System.out.println(e);
        System.out.println("finish");
    }


    @Test
    public void testSwitch() {
        int i = 12;
        switch (i) {
            case 12:
                System.out.println(12);
                break;
            case 13:
                System.out.println(13);
                break;
            default:
                System.out.println(14);
        }
    }


    @Test
    public void testLocalAddr() {
        System.out.println(RemotingUtil.getLocalAddress());
    }


    @Test
    public void testClazz() {
        int i = 12;
        System.out.println(new Object[]{i}[0].getClass().getTypeName());


        System.out.println(int.class.getName());
    }


    @Test
    public void testGson() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        RpcInvocation invocation = new RpcInvocation();
        invocation.setClassName("name");
        String data = gson.toJson(invocation);
        System.out.println(data);


        int i = 12;
        String str = new Gson().toJson(i);
        System.out.println(str);

        int i2 = new Gson().fromJson(str, int.class);
        System.out.println(i2);
    }


    @Test
    public void testHessian() {
        Object[] obj = new Object[]{1, 2, 3, 4, "a", 1.0, new A()};
        byte[] data = HessianUtils.write(obj);
        System.out.println(Arrays.toString(data));

        Object[] obj2 = (Object[]) HessianUtils.read(data);
        System.out.println(Arrays.toString(obj2));

    }

}
