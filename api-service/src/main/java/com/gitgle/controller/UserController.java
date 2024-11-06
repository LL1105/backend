package com.gitgle.controller;


import cn.dev33.satoken.util.SaResult;
import com.gitgle.result.Result;
import com.gitgle.service.TalentRankService;
import com.gitgle.service.UserService;
import com.gitgle.service.req.*;
import org.apache.dubbo.config.annotation.DubboReference;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
public class UserController {


    @DubboReference
    private UserService userService;

    @DubboReference
    private TalentRankService talentRankService;


    /**
     * 发送注册邮件
     * @param email
     * @return
     */
    @GetMapping("/sendEmail")
    public Result sendEmail(@RequestParam("email") String email) {
        return userService.sendMimeMail(email);
    }

    /**
     * 注册
     * @param req
     * @return
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterReq req) {
        return  userService.register(req);
    }

    /**
     * 登录
     * @param loginReq
     * @return
     * @throws Exception
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginReq loginReq) throws Exception {
        return userService.login(loginReq.getEmail(), loginReq.getPassword());
    }

    /**
     * 退出登录
     * @return
     */
    @PostMapping("/logout")
    public SaResult logout() {
        return userService.logout();
    }

    /**
     * 展示个人信息
     * @return
     */
    @PostMapping("/getUserInfo")
    public Result getUserInfo() {
        return userService.getUserInfo();
    }

    @PostMapping("/getRank")
    public Result getTalentRank(Integer userId) {
        return Result.Success(talentRankService.getTalentrankByDeveloperId(String.valueOf(userId)));
    }

    @PostMapping("/search")
    public Result search(@RequestParam("page") Integer page, @RequestParam("size") Integer size , @RequestBody SearchReq req) {
        return userService.search(page, size, req);
    }

    /**
     * 展示国家列表
     * @return
     */
    @PostMapping("/nation")
    public Result search() {
        return userService.getNation();
    }

    /**
     * 展示开发者详情
     * @param githubId
     * @return
     */
    @PostMapping("/detail")
    public Result showGithubUserInfo(@RequestParam("githubId") String githubId) {
        return userService.showUserInfo(githubId);
    }

    /**
     * 修改个人信息
     * @param req
     * @return
     */
    @PostMapping("/change")
    public Result changeUserInfo(@RequestBody ChangeUserInfoReq req) {
        return userService.changeUserInfo(req);
    }

    /**
     * 修改用户密码
     * @param req
     * @return
     */
    @PostMapping("/changePassword")
    public Result changePassword(@RequestBody ChangePasswordReq req) {
        return userService.changePassword(req);
    }

}
