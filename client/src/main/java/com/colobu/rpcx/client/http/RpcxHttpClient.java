package com.colobu.rpcx.client.http;

import com.colobu.rpcx.common.Pair;
import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.rpc.HessianUtils;
import com.colobu.rpcx.rpc.Invocation;
import com.colobu.rpcx.rpc.URL;
import com.colobu.rpcx.rpc.impl.RpcInvocation;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RpcxHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(RpcxHttpClient.class);


    public static String execute(String url, String service, String method, Pair<String, Object>... params) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);
            post.setHeader("X-RPCX-ServicePath", service);
            post.setHeader("X-RPCX-ServiceMethod", method);

            Invocation invocation = new RpcInvocation();
            String[] parameterTypeNames = new String[params.length];
            Object[] arguments = new Object[params.length];
            IntStream.range(0, params.length).forEach(index -> {
                Pair<String, Object> p = params[index];
                parameterTypeNames[index] = p.getObject1();
                arguments[index] = p.getObject2();
            });

            invocation.setParameterTypeNames(parameterTypeNames);
            invocation.setArguments(arguments);

            URL u = new URL("rpcx", "", 0);
            Gson gson = new Gson();

            ((RpcInvocation) invocation).setClassName(service);
            ((RpcInvocation) invocation).setMethodName(method);

            u.setServiceInterface(invocation.getClassName() + "" + invocation.getMethodName());
            String _params = Stream.of(invocation.getArguments()).map(it -> gson.toJson(it)).collect(Collectors.joining(","));
            u.setPath(invocation.getClassName() + "." + invocation.getMethodName() + "(" + _params + ")");
            ((RpcInvocation) invocation).setUrl(u);


            byte[] payload = HessianUtils.write(invocation);

            ByteArrayEntity entriy = new ByteArrayEntity(payload, ContentType.DEFAULT_BINARY);
            post.setEntity(entriy);
            HttpResponse res = client.execute(post);
            HttpEntity resEntity = res.getEntity();
            byte[] data = EntityUtils.toByteArray(resEntity);
            logger.info("res:" + new String(data));

            Message message = gson.fromJson(new String(data), Message.class);
            ((DefaultHttpClient) client).close();
            return new String(message.payload);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }
}
