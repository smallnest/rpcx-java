package com.colobu.rpcx.handler;

import com.colobu.rpcx.service.ITestService;
import com.colobu.rpcx.spring.RpcxConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * @return
     */
    @GetMapping("/generic")
    public String generic() {
        Object s = consumer.invoke("com.colobu.rpcx.service.TestService", "sum", new String[]{"int","int"}, new String[]{"11","22"});
        return s.toString();
    }

}
