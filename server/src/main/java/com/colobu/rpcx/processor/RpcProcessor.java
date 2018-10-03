package com.colobu.rpcx.processor;

import com.colobu.rpcx.common.ClassUtils;
import com.colobu.rpcx.common.NetUtils;
import com.colobu.rpcx.common.StringUtils;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.FilterWrapper;
import com.colobu.rpcx.netty.NettyRequestProcessor;
import com.colobu.rpcx.protocol.LanguageCode;
import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.MessageType;
import com.colobu.rpcx.protocol.RemotingCommand;
import com.colobu.rpcx.rpc.*;
import com.colobu.rpcx.rpc.annotation.Provider;
import com.colobu.rpcx.rpc.impl.Exporter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.colobu.rpcx.rpc.impl.RpcProviderInvoker;
import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author goodjava@qq.com
 * 处理远程rpc调用
 */
public class RpcProcessor implements NettyRequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RpcProcessor.class);

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {
        request.decode();

        String language = request.getMessage().metadata.get(Constants.LANGUAGE);
        String className = request.getMessage().servicePath;
        String methodName = request.getMessage().serviceMethod;
        RpcInvocation invocation = null;
        //golang调用是没有language的
        if (null == language) {
            invocation = new RpcInvocation();
            invocation.setLanguageCode(LanguageCode.GO);
            invocation.url = new URL("rpcx", "", 0);
            invocation.url.setPath(className + "." + methodName);
            //golang 参数是byte[]
            invocation.setParameterTypeNames(new String[]{"byte[]"});
            //参数就是payload数据
            invocation.setArguments(new Object[]{request.getMessage().payload});
        } else if (LanguageCode.JAVA.name().equals(language)) {
            invocation = (RpcInvocation) HessianUtils.read(request.getMessage().payload);
            invocation.url.setHost(request.getMessage().metadata.get("_host"));
            invocation.url.setPort(Integer.parseInt(request.getMessage().metadata.get("_port")));
        }

        invocation.setClassName(className);
        invocation.setMethodName(methodName);

        String key = ClassUtils.getMethodKey(className, methodName, invocation.getParameterTypeNames());

        Invoker<Object> wrapperInvoker = Exporter.invokerMap.get(key);

        Result rpcResult = wrapperInvoker.invoke(invocation);

        RemotingCommand res = RemotingCommand.createResponseCommand(new Message(invocation.getClassName(), invocation.getMethodName(), MessageType.Response, request.getOpaque()));

        if (invocation.languageCode.equals(LanguageCode.HTTP)) {
            res.getMessage().payload = new Gson().toJson(rpcResult.getValue()).getBytes();
        } else if (invocation.languageCode.equals(LanguageCode.JAVA)) {
            res.getMessage().payload = HessianUtils.write(rpcResult.getValue());
        } else {
            res.getMessage().payload = (byte[]) rpcResult.getValue();
        }
        if (rpcResult.hasException()) {
            logger.error(rpcResult.getException().getMessage(), rpcResult.getException());
            res.getMessage().metadata.put(Constants.RPCX_ERROR_CODE, "-2");
            res.getMessage().metadata.put(Constants.RPCX_ERROR_MESSAGE, rpcResult.getException().getMessage());
        }
        return res;
    }



    @Override
    public boolean rejectRequest() {
        return false;
    }


}
