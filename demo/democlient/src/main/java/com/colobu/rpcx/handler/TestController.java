package com.colobu.rpcx.handler;

import com.colobu.rpcx.service.IArith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private IArith arith;

    @Autowired
    private ApplicationContext context;

    @GetMapping("/input")
    public String input(String word) {
        String s = arith.hi(word);
        return s;
    }

}
