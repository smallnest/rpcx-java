package com.colobu.rpcx.springboot;

import com.colobu.rpcx.service.IArith;
import com.colobu.rpcx.service.ITestService;
import com.colobu.rpcx.spring.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.colobu.rpcx"})
@RestController
public class Bootstrap {


    public static void main(String... args) {

        SpringApplication.run(Bootstrap.class, args);
    }

    @Autowired
    private ExampleService exampleService;

    //    @Autowired
//    @Qualifier("testService")
    private ITestService testService;


//    @Autowired
    private String str;


    @Autowired
    private IArith arith;


    @Autowired
    private ApplicationContext context;

    @GetMapping("/input")
    public String input(String word) {
        String s = arith.hi("aaa");
        System.out.println(s);
//        System.out.println(testService.test());
        Object obj = context.getBean("str");
        System.out.println(str + obj);
        return exampleService.wrap(word + s);
    }
}
