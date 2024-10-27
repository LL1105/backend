package com.gitgle.service.impl;

import com.gitgle.response.DomainResponse;
import com.gitgle.service.DomainService;
import com.gitgle.utils.SparkApiUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.io.IOException;

@DubboService
public class DomainServiceImpl implements DomainService {

    @Resource
    private SparkApiUtils sparkApiUtils;

    @Override
    public DomainResponse getDomainByDeveloperId(String owner) {
        String question = "根据以上信息，请你分析该信息对应的开发者的专业领域和编程语言，以下是有且仅有的专业领域和编程语言信息，你必须从这之中选择出来并以|分隔：";
        try {
            okhttp3.Response response = sparkApiUtils.doRequest(question);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new DomainResponse();
    }
}
