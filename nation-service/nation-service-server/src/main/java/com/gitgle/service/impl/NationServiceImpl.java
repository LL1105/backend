package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.response.*;
import com.gitgle.result.RpcResult;
import com.gitgle.service.*;
import com.gitgle.utils.SparkApiUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@DubboService
@Slf4j
public class NationServiceImpl implements NationService {

    @Resource
    private NationCalculationService nationCalculationService;

    @Override
    public RpcResult<NationResponse> getNationByDeveloperId(String login) {
        RpcResult<NationResponse> nationResponseRpcResult = new RpcResult<>();
        try {
            NationResponse nationResponse = nationCalculationService.calculateNation(login);
            nationResponseRpcResult.setData(nationResponse);
            nationResponseRpcResult.setCode(RpcResultCode.SUCCESS);
            return nationResponseRpcResult;
        } catch (Exception e) {
            nationResponseRpcResult.setCode(RpcResultCode.FAILED);
            return nationResponseRpcResult;
        }
    }
}
