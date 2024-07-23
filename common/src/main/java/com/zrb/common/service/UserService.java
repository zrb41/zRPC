package com.zrb.common.service;


import com.zrb.common.model.User;

public interface UserService {
    User getUser(User user);

    Integer fun(Integer x);
    default Integer getNumber(){
        return 6;
    }
}
