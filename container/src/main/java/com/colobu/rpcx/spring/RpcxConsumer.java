package com.colobu.rpcx.spring;

import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.FilterWrapper;
import com.colobu.rpcx.netty.IClient;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.ReflectUtils;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.RpcException;
import com.colobu.rpcx.rpc.impl.ConsumerConfig;
import com.colobu.rpcx.rpc.impl.RpcConsumerInvoker;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author goodjava@qq.com
 */
public class RpcxConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RpcxConsumer.class);


    private final IClient client;

    public RpcxConsumer(IClient client) {
        this.client = client;
    }

    /**
     * 全部使用默认配置
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T wrap(Class clazz) {
        return (T) new ConsumerConfig(client).refer(clazz);
    }

    /**
     * 可以传入配置 如超时时间等
     *
     * @param clazz
     * @param builder
     * @param <T>
     * @return
     */
    public <T> T wrap(Class clazz, ConsumerConfig.ConsumerConfigBuilder builder) {
        return (T) builder.setClient(client).build().refer(clazz);
    }

    /**
     * 泛化调用
     * params 是参数的json字符串形式
     * 在不依赖jar的情况下即可调用
     */
    public String invoke(String className, String methodName, String[] types, String[] params) {
        RpcInvocation invocation = new RpcInvocation();
        invocation.setMethodName(Constants.$INVOKE);
        invocation.setClassName(className);
        invocation.setParameterTypeNames(types);
        String[] params1 = new String[params.length + 1];
        params1[0] = methodName;
        System.arraycopy(params, 0, params1, 1, params.length);
        invocation.setArguments(params1);

        Invoker invoker = new RpcConsumerInvoker(client);
        Invoker<Object> wrapperInvoker = FilterWrapper.ins().buildInvokerChain(invoker, "", Constants.CONSUMER, Sets.newHashSet());

        Result result = wrapperInvoker.invoke(invocation);
        if (result.hasException()) {
            throw new RpcException(result.getException());
        }
        return result.getValue().toString();
    }

    /**
     * echo调用
     * 测试服务是否可连通
     */
    public String echo(String className, String str) {
        RpcInvocation invocation = new RpcInvocation();
        invocation.setMethodName(Constants.$ECHO);
        invocation.setClassName(className);
        invocation.setParameterTypeNames(new String[]{ReflectUtils.getName(String.class)});
        invocation.setArguments(new String[]{str});
        RpcConsumerInvoker invoker = new RpcConsumerInvoker(client);
        Invoker<Object> wrapperInvoker = FilterWrapper.ins().buildInvokerChain(invoker, "", Constants.CONSUMER, Sets.newHashSet());
        Result result = wrapperInvoker.invoke(invocation);
        if (result.hasException()) {
            throw new RuntimeException(result.getException());
        }
        return result.getValue().toString();
    }


    /**
     * 热部署
     * 动态修改class bytecode
     */
    public String deploy(String className, String classPath, String token) throws IOException {
        RpcInvocation invocation = new RpcInvocation();
        invocation.setMethodName(Constants.$HOT_DEPLOY);
        invocation.setClassName(className);

        String[] paramNames = new String[3];
        paramNames[0] = ReflectUtils.getName(String.class);
        paramNames[1] = ReflectUtils.getName(String.class);
        paramNames[2] = ReflectUtils.getName(byte[].class);
        invocation.setParameterTypeNames(paramNames);
        byte[] classData = Files.readAllBytes(Paths.get(classPath));
        Object[] params = new Object[]{className, token, classData};
        invocation.setArguments(params);
        RpcConsumerInvoker invoker = new RpcConsumerInvoker(client);
        Invoker<Object> wrapperInvoker = FilterWrapper.ins().buildInvokerChain(invoker, "", Constants.CONSUMER, Sets.newHashSet());
        Result result = wrapperInvoker.invoke(invocation);
        if (result.hasException()) {
            throw new RuntimeException(result.getException());
        }
        return result.getValue().toString();
    }

    public void close() {
        logger.info("==================>close");
        this.client.close();
    }

    public IClient getClient() {
        return client;
    }
}
