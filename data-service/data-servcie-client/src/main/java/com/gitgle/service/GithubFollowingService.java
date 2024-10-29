package com.gitgle.service;

import com.gitgle.response.GithubFollowersResponse;
import com.gitgle.response.GithubFollowingResponse;
import com.gitgle.result.RpcResult;

public interface GithubFollowingService {

    /**
     * 根据开发者id查询关注用户的用户列表
     * @param login 开发者login
     * @return 关注列表
     */
    RpcResult<GithubFollowersResponse> getFollowersByDeveloperId(String login);

    /**
     * 根据开发者id查询被用户关注的用户列表
     * @param login 开发者login
     * @return 被关注列表
     */
    RpcResult<GithubFollowingResponse> listUserFollowingByDeveloperId(String login);

}
