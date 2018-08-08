package com.colobu.rpcx.processor;

import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.filter.FilterWrapper;
import com.colobu.rpcx.netty.NettyRequestProcessor;
import com.colobu.rpcx.protocol.LanguageCode;
import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.MessageType;
import com.colobu.rpcx.protocol.RemotingCommand;
import com.colobu.rpcx.rpc.*;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.colobu.rpcx.rpc.impl.RpcProviderInvoker;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author goodjava@qq.com
 * 处理远程rpc调用
 */
public class RpcProcessor implements NettyRequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RpcProcessor.class);

    private final Function<Class, Object> getBeanFunc;

    public RpcProcessor(Function<Class, Object> getBeanFunc) {
        this.getBeanFunc = getBeanFunc;
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {
        String language = request.getMessage().metadata.get("language");
        RpcInvocation invocation = null;

        String className = request.getMessage().servicePath;
        String methodName = request.getMessage().serviceMethod;

        //golang调用是没有language的
        if (null == language) {
            invocation = new RpcInvocation();
            invocation.setLanguageCode(LanguageCode.GO);
            invocation.url = new URL("rpcx", "", 0);
            invocation.url.setPath(className+"."+methodName);
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

        Invoker<Object> invoker = new RpcProviderInvoker<>(getBeanFunc);
        Invoker<Object> wrapperInvoker = FilterWrapper.ins().buildInvokerChain(invoker, "", Constants.PROVIDER);

        RemotingCommand res = RemotingCommand.createResponseCommand();
        Message resMessage = new Message();
        resMessage.servicePath = invocation.getClassName();
        resMessage.serviceMethod = invocation.getMethodName();
        resMessage.setMessageType(MessageType.Response);
        resMessage.setSeq(request.getOpaque());

        Result rpcResult = wrapperInvoker.invoke(invocation);
        if (invocation.languageCode.equals(LanguageCode.HTTP)) {
            resMessage.payload = new Gson().toJson(rpcResult.getValue()).getBytes();
        } else if (invocation.languageCode.equals(LanguageCode.JAVA)) {
            resMessage.payload = HessianUtils.write(rpcResult.getValue());
        } else {
            resMessage.payload = (byte[]) rpcResult.getValue();
        }
        if (rpcResult.hasException()) {
            logger.error(rpcResult.getException().getMessage(), rpcResult.getException());
            resMessage.metadata.put(Constants.RPCX_ERROR_CODE, "-2");
            resMessage.metadata.put(Constants.RPCX_ERROR_MESSAGE, rpcResult.getException().getMessage());
        }
        res.setMessage(resMessage);
        return res;
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }
}
