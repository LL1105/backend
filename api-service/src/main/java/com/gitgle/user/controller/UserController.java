package com.gitgle.user.controller;


import cn.dev33.satoken.util.SaResult;
import com.gitgle.result.R;
import com.gitgle.service.UserService;
import com.gitgle.service.VO.LoginVO;
import com.gitgle.service.VO.UserVo;
import org.apache.dubbo.config.annotation.DubboReference;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
public class UserController {


    @DubboReference
    private UserService userService;


    @GetMapping("/sendEmail")
    public R sendEmail(@RequestParam("email") String email) {
        return userService.sendMimeMail(email);
    }

    @PostMapping("/register")
    public R register(@RequestBody UserVo userVo) {
        return  userService.register(userVo);
    }

    @PostMapping("/login")
    public SaResult login(@RequestBody LoginVO loginVO) throws Exception {
        return userService.login(loginVO.getEmail(), loginVO.getPassword());
    }

    @PostMapping("/logout")
    public SaResult logout() {
        return userService.logout();
    }

    @PostMapping("/auth")
    public R auth() {
        return R.Success("auth success");
    }

}
