package com.gitgle.service;

import com.gitgle.response.GithubFollowers;
import com.gitgle.response.GithubFollowersResponse;
import com.gitgle.response.GithubUser;
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
     * 根据开发者id查询关注列表
     * @param developerId 开发者login
     * @return 关注列表
     */
    RpcResult<GithubFollowersResponse> getFollowersByDeveloperId(String developerId);
}
