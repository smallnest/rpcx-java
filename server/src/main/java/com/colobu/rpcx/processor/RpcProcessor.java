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
        Message req = request.getMessage();
        String language = req.metadata.get("language");
        RpcInvocation invocation = null;
        //golang 调用(自己组装invocation)
        if (null == language) {
            Message reqMsg = request.getMessage();
            invocation = new RpcInvocation();
            invocation.setClassName(reqMsg.servicePath);
            invocation.setMethodName(reqMsg.serviceMethod);
            //golang 参数是byte[]
            invocation.setParameterTypeNames(new String[]{"[B"});
            //参数就是payload数据
            invocation.setArguments(new Object[]{request.getMessage().payload});
            invocation.url = new URL("rpcx", "", 0);
            invocation.languageCode = LanguageCode.GO;
        } else if (LanguageCode.JAVA.name().equals(language)) {
            invocation = (RpcInvocation) HessianUtils.read(request.getMessage().payload);
            invocation.languageCode = LanguageCode.valueOf(language);
        }

        invocation.servicePath = request.getMessage().servicePath;
        invocation.serviceMethod = request.getMessage().serviceMethod;
        invocation.url.setHost(req.metadata.get("_host"));
        invocation.url.setPort(Integer.parseInt(req.metadata.get("_port")));

        Invoker<Object> invoker = new RpcProviderInvoker<>(getBeanFunc);
        Invoker<Object> wrapperInvoker = FilterWrapper.ins().buildInvokerChain(invoker, "", Constants.PROVIDER);

        RemotingCommand res = RemotingCommand.createResponseCommand();

        Message resMessage = new Message();
        resMessage.servicePath = invocation.servicePath;
        resMessage.serviceMethod = invocation.serviceMethod;
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
            resMessage.metadata.put("_rpcx_error_code", "-2");
            resMessage.metadata.put("_rpcx_error_message", rpcResult.getException().getMessage());
        }
        res.setMessage(resMessage);
        return res;
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }
}
