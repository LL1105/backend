package com.gitgle.service;

import com.gitgle.response.*;
import com.gitgle.result.RpcResult;

import java.util.Map;

/**
 * Github Api 用户接口
 */
public interface GithubUserService {

    /**
     * 搜索开发者
     * @param searchParams 查询参数
     * @return 开发者信息
     */
    RpcResult<GithubUserResponse> searchUsers(Map<String, String> searchParams);

    /**
     * 根据开发者id查询组织列表
     * @param developerId 开发者id
     * @return 组织列表
     */
    RpcResult<GithubOrganizationResponse> getOrganizationByDeveloperId(String developerId);

    /**
     * 通过github用户id查询github用户信息
     * @param login 用户login
     * @return 用户信息
     */
    RpcResult<GithubUser> getUserByLogin(String login);

    RpcResult<GithubUser> listUserByFollowers();
}
