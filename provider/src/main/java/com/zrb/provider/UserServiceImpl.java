package com.zrb.provider;

import com.zrb.common.model.User;
import com.zrb.common.service.UserService;
import com.zrb.zrpcspringbootstarter.annotation.RpcService;
import org.springframework.stereotype.Service;

@Service
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        return null;
    }

    @Override
    public Integer fun(Integer x) {
        return x+1;
    }
}
