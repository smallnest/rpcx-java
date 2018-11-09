package com.colobu.rpcx.processor;

import com.colobu.rpcx.common.ClassUtils;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.netty.NettyRequestProcessor;
import com.colobu.rpcx.protocol.LanguageCode;
import com.colobu.rpcx.protocol.RemotingCommand;
import com.colobu.rpcx.protocol.RemotingSysResponseCode;
import com.colobu.rpcx.rpc.HessianUtils;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.URL;
import com.colobu.rpcx.rpc.impl.Exporter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * @author goodjava@qq.com
 * 处理远程rpc调用
 */
public class RpcProcessor implements NettyRequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RpcProcessor.class);

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws UnsupportedEncodingException {
        request.getMessage().decode(request.getData());

        String language = request.getMessage().metadata.get(Constants.LANGUAGE);
        String className = request.getMessage().servicePath;
        String methodName = request.getMessage().serviceMethod;
        RpcInvocation invocation = null;
        //golang调用是没有language的
        if (null == language) {
            //golang 是没有invocation的
            invocation = new RpcInvocation();
            invocation.setClassName(className);
            invocation.setMethodName(methodName);
            invocation.setLanguageCode(LanguageCode.GO);
            invocation.setParameterTypeNames(new String[]{"byte[]"});
            String path = ClassUtils.getMethodKey(className, methodName, new String[]{"byte[]"});
            invocation.url = new URL("rpcx", "", 0, path);
            //golang 参数是byte[]
            invocation.setParameterTypeNames(new String[]{"byte[]"});
            //参数就是payload数据
            invocation.setArguments(new Object[]{request.getMessage().payload});
        } else if (LanguageCode.JAVA.name().equals(language)) {
            invocation = (RpcInvocation) HessianUtils.read(request.getMessage().payload);
        }

        Invoker<Object> wrapperInvoker = null;

        //request -> response
        RemotingCommand res = request.requestToResponse();

        String key = "";


        if (methodName.equals(Constants.$ECHO)) {
            //echo
            key = Constants.$ECHO;
        } else if (methodName.equals(Constants.$INVOKE)) {
            //泛化调用
            key = Constants.$INVOKE;
        } else {
            key = ClassUtils.getMethodKey(className, methodName, invocation.getParameterTypeNames());
        }

        wrapperInvoker = Exporter.invokerMap.get(key);

        if (null == wrapperInvoker) {
            logger.warn("get invoker is null key:{}", key);
            res.setErrorMessage(RemotingSysResponseCode.SYSTEM_ERROR, "get invoker is null  key:" + key);
            return res;
        }

        Result rpcResult = wrapperInvoker.invoke(invocation);
        res.getMessage().metadata.put(Constants.TRACE_ID, rpcResult.getAttachment(Constants.TRACE_ID, ""));

        if (invocation.languageCode.equals(LanguageCode.JAVA)) {
            res.getMessage().payload = HessianUtils.write(rpcResult.getValue());
        } else {
            res.getMessage().payload = (byte[]) rpcResult.getValue();
        }
        if (rpcResult.hasException()) {
            logger.error(rpcResult.getException().getMessage(), rpcResult.getException());
            res.setErrorMessage(RemotingSysResponseCode.SYSTEM_ERROR, rpcResult.getException().getMessage());
        }
        return res;
    }


    @Override
    public boolean rejectRequest() {
        return false;
    }


}
