package com.gitgle.user.controller;


import cn.dev33.satoken.util.SaResult;
import com.gitgle.result.R;
import com.gitgle.service.TalentRankService;
import com.gitgle.service.UserService;
import com.gitgle.service.vo.req.LoginReq;
import com.gitgle.service.vo.UserVo;
import com.gitgle.service.vo.req.SearchReq;
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
    public R sendEmail(@RequestParam("email") String email) {
        return userService.sendMimeMail(email);
    }

    @PostMapping("/register")
    public R register(@RequestBody UserVo userVo) {
        return  userService.register(userVo);
    }

    @PostMapping("/login")
    public R login(@RequestBody LoginReq loginReq) throws Exception {
        return userService.login(loginReq.getEmail(), loginReq.getPassword());
    }

    @PostMapping("/logout")
    public SaResult logout() {
        return userService.logout();
    }

    @PostMapping("/auth")
    public R auth() {
        return R.Success("auth success");
    }

    @PostMapping("/getUserInfo")
    public R getUserInfo() {
        return userService.getUserInfo();
    }

    @PostMapping("/getRank")
    public R getTalentRank(Integer userId) {
        return R.Success(talentRankService.getTalentrankByDeveloperId(String.valueOf(userId)));
    }

    @PostMapping("/search")
    public R search(@RequestBody SearchReq req) {
        return userService.search(req);
    }

    @PostMapping("/nation")
    public R search() {
        return userService.getNation();
    }

}
