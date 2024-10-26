package com.gitgle.service;

import com.gitgle.constant.RpcResultCode;
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
}
