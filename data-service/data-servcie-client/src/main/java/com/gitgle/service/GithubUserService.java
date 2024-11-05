package com.gitgle.service;

import com.gitgle.response.*;
import com.gitgle.result.RpcResult;

import java.util.Map;

/**
 * Github Api 用户接口
 */
public interface GithubUserService {

    /**
     * 通过github用户id查询github用户信息
     * @param login 用户login
     * @return 用户信息
     */
    RpcResult<GithubUser> getUserByLogin(String login);
}
