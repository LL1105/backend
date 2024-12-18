package com.gitgle.service;

import cn.dev33.satoken.util.SaResult;
import com.gitgle.result.Result;
import com.gitgle.result.RpcResult;
import com.gitgle.service.req.*;


public interface UserService {

    String getRank(Integer userId);

    Result getUserInfo();

    //发送验证码邮件给指定邮箱
    Result sendMimeMail(String email);

    //用户注册，验证验证码并保存用户信息
    Result register(RegisterReq req);

    //登录
    Result login(String email, String password) throws Exception;

    //登出
    SaResult logout();

    //搜索用户
    Result search(Integer page, Integer size, SearchReq req);

    Result getNation();

    Result changeUserInfo(ChangeUserInfoReq req);

    Result showUserInfo(String login);

    Result changePassword(ChangePasswordReq req);

    /**
     * 根据领域id获取用户数量
     */
    RpcResult<Long> getUserCountInDomain(Integer domainId);


}
