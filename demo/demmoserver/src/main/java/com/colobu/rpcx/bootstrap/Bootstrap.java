package com.colobu.rpcx.bootstrap;

import com.colobu.rpcx.service.IArith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author goodjava@qq.com
 */
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.colobu.rpcx"})
@RestController
public class Bootstrap {

    public static void main(String... args) {
        SpringApplication.run(Bootstrap.class, args);
    }

    @Autowired
    private IArith arith;


    @GetMapping("/sum")
    public String sum(String word) {
        String s = String.valueOf(arith.sum(11, 22));
        return s;
    }
}
