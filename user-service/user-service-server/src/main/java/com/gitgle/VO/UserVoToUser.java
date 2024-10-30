package com.gitgle.VO;


import com.gitgle.entity.User;
import com.gitgle.service.vo.UserVo;

public class UserVoToUser {

    public static User toUser(UserVo userVo) {
        User user = new User();
        user.setUsername(userVo.getUsername());
        user.setPassword(userVo.getPassword());
        user.setEmail(userVo.getEmail());
        return user;
    }
}
