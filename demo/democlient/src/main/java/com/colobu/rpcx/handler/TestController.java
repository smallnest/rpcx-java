package com.colobu.rpcx.handler;

import com.colobu.rpcx.netty.ResponseFuture;
import com.colobu.rpcx.rpc.RpcContext;
import com.colobu.rpcx.service.ITestService;
import com.colobu.rpcx.spring.RpcxConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
public class TestController {

    @Autowired
    private ITestService testService;

    @Autowired
    private ApplicationContext context;


    @Autowired
    private RpcxConsumer consumer;


    //curl "http://127.0.0.1:8015/hi?word=abc"
    @GetMapping("/hi")
    public String input(String word) {
        String s = testService.hi(word);
        return s;
    }

    /**
     * 测试echo
     *
     * @param word
     * @return
     */
    @GetMapping("/echo")
    public String echo(String word) {
        String s = testService.$echo(word);
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
     * 异步模式
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/async")
    public String async() throws InterruptedException {
        testService.hi("abc");
        ResponseFuture<String> f1 = RpcContext.getContext().getFuture();

//        String s = f1.get();
//        System.out.println(s);
        System.out.println(111111111111L);
        testService.hi("def");
        ResponseFuture<String> f2 = RpcContext.getContext().getFuture();
//        s = f2.get();
//        System.out.println(s);
        System.out.println(2222222222L);
        String s1 = f1.get(6000);
        String s2 = f2.get(6000);
        String result = s1 + s2;
        return result;
    }


}
