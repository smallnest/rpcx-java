package com.colobu.rpcx.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;


/**
 * @author goodjava@qq.com
 */
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.colobu.rpcx"})
public class Bootstrap {

    public static void main(String... args) {
        SpringApplication.run(Bootstrap.class, args);
    }

}
