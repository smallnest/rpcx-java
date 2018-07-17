package com.colobu.rpcx.handler;

import com.colobu.rpcx.service.IArith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {


    public TestController() {
        System.out.println("-----------------1111");
    }

    @Autowired
    private IArith arith;


    //    @Autowired
    private String str;


    @Autowired
    private ApplicationContext context;


    @GetMapping("/input")
    public String input(String word) {
//        IArith arith = context.getBean(IArith.class);
        String s = arith.hi("aaa");
        System.out.println(str);
        return s;
    }

}
