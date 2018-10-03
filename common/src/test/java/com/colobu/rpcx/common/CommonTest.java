package com.colobu.rpcx.common;

import com.colobu.rpcx.common.retry.RetryNTimes;
import com.colobu.rpcx.common.retry.RetryPolicy;
import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.RemotingCommand;
import com.colobu.rpcx.rpc.A;
import com.colobu.rpcx.rpc.HessianUtils;
import com.colobu.rpcx.rpc.ReflectUtils;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.Test;
import sun.reflect.MethodAccessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by goodjava@qq.com.
 */
public class CommonTest {

    //2540 20153
    @Test
    public void testMap2() {
        ExecutorService pool = Executors.newFixedThreadPool(20);

        ConcurrentHashMap<String,String>m2 = new ConcurrentHashMap<>();
        m2.put("a","1");

        Stopwatch sw2 = Stopwatch.createStarted();
        IntStream.range(0,1000000).forEach(i->{
            try {
                pool.invokeAll(IntStream.range(0,20).mapToObj(ii->(Callable<Void>)()->{
                    m2.get("a");
                    return null;
                }).collect(Collectors.toList()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println(sw2.elapsed(TimeUnit.MILLISECONDS));

    }

    //2554 22076
    @Test
    public void testMap1() {
        ExecutorService pool = Executors.newFixedThreadPool(20);

        Map<String,String> m = new HashMap<>();
        m.put("a","1");
        Stopwatch sw = Stopwatch.createStarted();
        IntStream.range(0,1000000).forEach(i->{
            try {
                pool.invokeAll(IntStream.range(0,20).mapToObj(ii->(Callable<Void>)()->{
                    m.get("a");
                    return null;
                }).collect(Collectors.toList()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println(sw.elapsed(TimeUnit.MILLISECONDS));




    }


    @Test
    public void testPool() throws Exception {
        Stopwatch sw = Stopwatch.createStarted();
        IntStream.range(0, 10000000).forEach(it -> {
            RemotingCommand.createRequestCommand(2);
        });
        System.out.println(sw.elapsed(TimeUnit.MILLISECONDS));

        TestPoolFactory factory = new TestPoolFactory();
        GenericObjectPool pool = new GenericObjectPool(factory);
        sw.reset();
        sw.start();

        pool.addObject();
        pool.addObject();
        pool.addObject();
        pool.addObject();
        pool.addObject();
        IntStream.range(0, 10000000).forEach(it -> {
            try {
                Object v = pool.borrowObject();
//                System.out.println(v);
                pool.returnObject( v);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.out.println(sw.elapsed(TimeUnit.MILLISECONDS));
    }


    @Test
    public void testBuffer() {
        byte[] b = new byte[]{0, 0, 0, 1, 0, 0, 0, 2};
        long begin = System.currentTimeMillis();
        IntStream.range(0, 1).forEach(it -> {
            ByteBuffer buffer = ByteBuffer.wrap(b);
            buffer.position(4);
            System.out.println(buffer.getInt());
        });
        System.out.println(System.currentTimeMillis() - begin);

        begin = System.currentTimeMillis();
        IntStream.range(0, 1).forEach(it -> {
            System.out.println(Bytes.bytes2int(b, 4));
        });
        System.out.println(System.currentTimeMillis() - begin);

    }


    @Test
    public void testMessage() {
        Message m = new Message();
        m.serviceMethod = "abc";
        m.clear();
        System.out.println(m.serviceMethod);
    }


    @Test
    public void testWrite() {

        Map<String, String> m = Maps.newHashMap();
        m.put("a", "aaaaaaaaaaaaaaaaaad");
        m.put("b", "aaaaaaaaaaaaaaaaaad");

        long now = System.currentTimeMillis();
        IntStream.range(0, 1).forEach(i -> {
            try {
                System.out.println(Arrays.toString(encodeMetadata(m)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        System.out.println(System.currentTimeMillis() - now);

        now = System.currentTimeMillis();

        IntStream.range(0, 1).forEach(i -> {
            try {
                System.out.println(Arrays.toString(encodeMetadata2(m)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        System.out.println(System.currentTimeMillis() - now);
    }


    private byte[] encodeMetadata(Map<String, String> metadata) throws IOException {
        if (metadata.size() == 0) {
            return new byte[]{};
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream(20);

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            byte[] keyBytes = key.getBytes("UTF-8");
            os.write(ByteBuffer.allocate(4).putInt(keyBytes.length).array());
            os.write(keyBytes);

            String v = entry.getValue();
            if (null == v) {
                v = "null";
            }
            byte[] vBytes = v.getBytes("UTF-8");
            os.write(ByteBuffer.allocate(4).putInt(vBytes.length).array());
            os.write(vBytes);
        }

        return os.toByteArray();
    }

    private byte[] encodeMetadata2(Map<String, String> metadata) throws IOException {
        if (metadata.size() == 0) {
            return new byte[]{};
        }

        ByteBuf buffer = Unpooled.buffer(20);

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            byte[] keyBytes = key.getBytes("UTF-8");
            buffer.writeInt(keyBytes.length);
            buffer.writeBytes(keyBytes);

            String v = entry.getValue();
            if (null == v) {
                v = "null";
            }
            byte[] vBytes = v.getBytes("UTF-8");
            buffer.writeInt(vBytes.length);
            buffer.writeBytes(vBytes);
        }


        return buffer.array();
    }


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

    //1702 1562 1537
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

    //1340 1244 1202
    @Test
    public void testReflect2() {
        Bean bean = new Bean();
        MethodAccess access = MethodAccess.get(Bean.class);
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            String r = (String) access.invoke(bean, "str", "zzy");
//            System.out.println(r);
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
