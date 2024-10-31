package com.gitgle.user.controller;


import cn.dev33.satoken.util.SaResult;
import com.gitgle.result.Result;
import com.gitgle.service.TalentRankService;
import com.gitgle.service.UserService;
import com.gitgle.service.req.LoginReq;
import com.gitgle.service.req.RegisterReq;
import com.gitgle.service.req.SearchReq;
import org.apache.dubbo.config.annotation.DubboReference;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
public class UserController {


    @DubboReference
    private UserService userService;

    @DubboReference
    private TalentRankService talentRankService;


    @GetMapping("/sendEmail")
    public Result sendEmail(@RequestParam("email") String email) {
        return userService.sendMimeMail(email);
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterReq req) {
        return  userService.register(req);
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginReq loginReq) throws Exception {
        return userService.login(loginReq.getEmail(), loginReq.getPassword());
    }

    @PostMapping("/logout")
    public SaResult logout() {
        return userService.logout();
    }

    @PostMapping("/auth")
    public Result auth() {
        return Result.Success("auth success");
    }

    @PostMapping("/getUserInfo")
    public Result getUserInfo() {
        return userService.getUserInfo();
    }

    @PostMapping("/getRank")
    public Result getTalentRank(Integer userId) {
        return Result.Success(talentRankService.getTalentrankByDeveloperId(String.valueOf(userId)));
    }

    @PostMapping("/search")
    public Result search(@RequestBody SearchReq req) {
        return userService.search(req);
    }

    @PostMapping("/nation")
    public Result search() {
        return userService.getNation();
    }

}
