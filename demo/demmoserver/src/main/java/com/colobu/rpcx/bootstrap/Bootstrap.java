package com.colobu.rpcx.bootstrap;

import com.colobu.rpcx.service.IArith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String... args) {
        try {
            SpringApplication.run(Bootstrap.class, args);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            System.exit(0);
        }
    }

    @Autowired
    private IArith arith;


    @GetMapping("/sum")
    public String sum() {
        String s = String.valueOf(arith.sum(11, 22));
        return s;
    }
}
