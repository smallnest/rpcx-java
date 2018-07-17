package com.colobu.rpcx.bootstrap;

import com.colobu.rpcx.service.IArith;
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

//    @Autowired
//    private IArith arith;


    @Autowired
    private ApplicationContext context;


    @GetMapping("/input")
    public String input(String word) {
        IArith arith = context.getBean(IArith.class);
        String s = arith.hi("aaa");
        return s;
    }
}
