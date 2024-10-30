package com.gitgle.service;

import com.gitgle.response.DomainResponse;
import com.gitgle.result.RpcResult;

public interface RpcDomainService {

    /**
     * 根据开发者login获取其所在领域
     * @param owner 开发者login
     * @return 领域集合
     */
    RpcResult<DomainResponse> getDomainByDeveloperId(String owner);
}
