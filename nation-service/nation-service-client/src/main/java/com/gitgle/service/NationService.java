package com.gitgle.service;

import com.gitgle.response.NationResponse;
import com.gitgle.result.RpcResult;

public interface NationService {

    /**
     * 根据开发者github用户名获取其Nation
     * @param login github用户名（login）
     * @return 开发者信息
     */
    RpcResult<NationResponse> getNationByDeveloperId(String login);
}
