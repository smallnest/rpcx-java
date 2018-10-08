package com.colobu.rpcx.handler;

import com.colobu.rpcx.netty.ResponseFuture;
import com.colobu.rpcx.rpc.RpcContext;
import com.colobu.rpcx.service.ITestService;
import com.colobu.rpcx.spring.RpcxConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author goodjava@qq.com
 */
@RestController
public class TestController {

    @Autowired
    private ITestService testService;

    @Autowired
    private ApplicationContext context;


    @Autowired
    private RpcxConsumer consumer;


    @GetMapping("/hi")
    public String input(String word) {
        String s = testService.hi(word);
        return s;
    }


    @GetMapping("/sum")
    public String sum() {
        String s = String.valueOf(testService.sum(22, 33));
        return s;
    }

    /**
     * 测试echo filter
     *
     * @return
     */
    @GetMapping("/echo")
    public String echo() {
        String s = consumer.echo("com.colobu.rpcx.service.TestService", "echo");
        return s;
    }

    /**
     * 泛化调用
     *
     * @return
     */
    @GetMapping("/generic")
    public String generic() {
        Object s = consumer.invoke("com.colobu.rpcx.service.TestService", "sum", new String[]{"int", "int"}, new String[]{"11", "22"});
        return s.toString();
    }

    /**
     * 异步模式(必须consumer 开启 异步模式)
     * ConsumerConfigBuilder.setSendType(Constants.ASYNC_KEY)
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/async")
    public String async() throws InterruptedException {
        testService.async();
        ResponseFuture<String> f1 = RpcContext.getContext().getFuture();
        RpcContext.removeContext();
        testService.async();
        ResponseFuture<String> f2 = RpcContext.getContext().getFuture();
        RpcContext.removeContext();
        String s1 = f1.get(2000);
        String s2 = f2.get(2000);
        String result = s1 + s2;
        return result;
    }

    /**
     * 测试热更新
     *
     * @return
     * @throws IOException
     */
    @GetMapping("/deploy")
    public String deploy() throws IOException {
        Object s = consumer.deploy("com.colobu.rpcx.service.TestService", "/Users/zhangzhiyong/IdeaProjects/rpcx-java/demo/demmoserver/target/classes/com/colobu/rpcx/service/TestService.class", "123abc");
        return s.toString();
    }


}
