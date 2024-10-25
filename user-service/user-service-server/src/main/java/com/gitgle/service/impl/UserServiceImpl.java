package com.gitgle.service.impl;

import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class UserServiceImpl implements com.gitgle.service.UserService{
    @Override
    public String getUserName() {
        return "";
    }
}
