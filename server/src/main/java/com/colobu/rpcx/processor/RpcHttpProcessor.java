package com.colobu.rpcx.processor;

import com.colobu.rpcx.common.ClassUtils;
import com.colobu.rpcx.config.Constants;
import com.colobu.rpcx.netty.NettyRequestProcessor;
import com.colobu.rpcx.protocol.*;
import com.colobu.rpcx.rpc.Invoker;
import com.colobu.rpcx.rpc.ReflectUtils;
import com.colobu.rpcx.rpc.Result;
import com.colobu.rpcx.rpc.URL;
import com.colobu.rpcx.rpc.impl.Exporter;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.stream.IntStream;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author goodjava@qq.com
 * 处理gateway过来的请求
 */
public class RpcHttpProcessor implements NettyRequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RpcHttpProcessor.class);

    private final Function<Class, Object> getBeanFunc;

    public RpcHttpProcessor(Function<Class, Object> getBeanFunc) {
        this.getBeanFunc = getBeanFunc;
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {
        Message req = request.getMessage();
        Gson gson = new Gson();
        RpcInvocation invocation = gson.fromJson(new String(request.getMessage().payload), RpcInvocation.class);
        String[] names = invocation.getParameterTypeNames();
        Object[] args = invocation.getArguments();
        invocation.setArguments(IntStream.range(0, names.length).mapToObj(i -> {
            String name = names[i];
            Class<?> clazz = ReflectUtils.forName(name);
            return gson.fromJson(args[i].toString(), clazz);
        }).toArray());
        invocation.languageCode = LanguageCode.HTTP;

        invocation.setClassName(request.getMessage().servicePath);
        invocation.setMethodName(request.getMessage().serviceMethod);
        invocation.setUrl(new URL("rpcx", "", 0));
        invocation.url.setHost(req.metadata.get("_host"));
        invocation.url.setPort(Integer.parseInt(req.metadata.get("_port")));


        Message resMessage = new Message();
        resMessage.servicePath = invocation.getClassName();
        resMessage.serviceMethod = invocation.getMethodName();
        resMessage.setMessageType(MessageType.Response);
        resMessage.setSeq(request.getOpaque());


        String key = ClassUtils.getMethodKey(invocation.getClassName(), invocation.getMethodName(), invocation.getParameterTypeNames());
        Invoker<Object> wrapperInvoker = Exporter.invokerMap.get(key);


        //提供的class+method+paramType 找不到 provider
        if (null == wrapperInvoker) {
            resMessage.metadata.put(Constants.RPCX_ERROR_CODE, String.valueOf(RemotingSysResponseCode.SYSTEM_ERROR));
            resMessage.metadata.put(Constants.RPCX_ERROR_MESSAGE, "don't find service:" + key);
            resMessage.setPayload(new byte[]{});
            byte[] data = new Gson().toJson(resMessage).getBytes();
            sendResponse(ctx, data);
            return RemotingCommand.createResponseCommand(resMessage);
        }


        Result rpcResult = wrapperInvoker.invoke(invocation);
        resMessage.metadata.put(Constants.TRACE_ID, rpcResult.getAttachment(Constants.TRACE_ID));
        resMessage.payload = new Gson().toJson(rpcResult.getValue()).getBytes();


        if (rpcResult.hasException()) {
            logger.error(rpcResult.getException().getMessage(), rpcResult.getException());
            resMessage.metadata.put(Constants.RPCX_ERROR_CODE, String.valueOf(RemotingSysResponseCode.SYSTEM_ERROR));
            resMessage.metadata.put(Constants.RPCX_ERROR_MESSAGE, rpcResult.getException().getMessage());
            resMessage.setPayload(new byte[]{});
        }

        RemotingCommand res = RemotingCommand.createResponseCommand(resMessage);

        logger.info("message:{}", res.getMessage());

        byte[] data = new Gson().toJson(res.getMessage()).getBytes();

        sendResponse(ctx, data);

        return res;
    }


    private void sendResponse(ChannelHandlerContext ctx, byte[] data) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                OK, Unpooled.wrappedBuffer(data));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH,
                response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, "close");
        ctx.write(response);
        ctx.flush();
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }
}
