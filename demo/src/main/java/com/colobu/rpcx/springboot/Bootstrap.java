package com.colobu.rpcx.springboot;

import com.colobu.rpcx.service.TestService;
import com.colobu.rpcx.spring.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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

    @Autowired
    private TestService testService;

    @GetMapping("/input")
    public String input(String word) {
        System.out.println(testService.test());
        return exampleService.wrap(word);
    }
}
