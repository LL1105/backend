package com.gitgle.user.controller;


import com.gitgle.result.R;
import com.gitgle.service.UserService;
import com.gitgle.service.VO.UserVo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
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

}
