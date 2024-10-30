package com.gitgle.service;

import cn.dev33.satoken.util.SaResult;
import com.gitgle.result.R;
import com.gitgle.service.VO.UserVo;
import com.gitgle.service.VO.req.RankReq;
import com.gitgle.service.VO.req.SearchReq;
import com.gitgle.service.VO.resp.SearchResp;

import java.util.List;


public interface UserService {

    String getRank(Integer userId);

    R getUserInfo();

    //发送验证码邮件给指定邮箱
    R sendMimeMail(String email);

    //用户注册，验证验证码并保存用户信息
    R register(UserVo userVo);

    //登录
    R login(String email, String password) throws Exception;

    //登出
    SaResult logout();

    R getUsersByNation(String nation);

    R conditionCheckRank(Integer size, Integer current, RankReq req);

    //搜索用户
    R search(SearchReq searchReq);

    R getNation();
}
