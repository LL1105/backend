package com.gitgle.service;

import com.gitgle.response.*;
import com.gitgle.result.RpcResult;

/**
 * Github Api 用户接口
 */
public interface GithubUserService {

    /**
     * 根据开发者id查询开发者信息
     * @param developerId 开发者id
     * @return 开发者信息
     */
    RpcResult<GithubUser> searchByDeveloperId(String developerId);

    /**
     * 根据开发者id查询关注用户的用户列表
     * @param developerId 开发者login
     * @return 关注列表
     */
    RpcResult<GithubFollowersResponse> getFollowersByDeveloperId(String developerId);

    /**
     * 根据开发者id查询被用户关注的用户列表
     * @param developerId 开发者id
     * @return 被关注列表
     */
    RpcResult<GithubFollowersResponse> listUserFollowingByDeveloperId(String developerId);

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
}
