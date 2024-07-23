package com.zrb.consumer;

import com.zrb.common.service.UserService;
import com.zrb.zrpcspringbootstarter.annotation.RpcReference;
import org.springframework.stereotype.Service;

@Service
public class Test {
    @RpcReference
    private UserService userService;

    public void test(int x){
        System.out.println(userService.fun(x));
    }

}
