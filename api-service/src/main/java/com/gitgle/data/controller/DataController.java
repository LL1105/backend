package com.gitgle.data.controller;

import com.gitgle.constant.RpcResultCode;
import com.gitgle.response.GithubDataResponse;
import com.gitgle.result.R;
import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubDataService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data")
public class DataController {

    @DubboReference
    private GithubDataService githubDataService;

    @GetMapping("/all")
    public R<GithubDataResponse> getAllGithubData(){
        RpcResult<GithubDataResponse> allGithubData = githubDataService.getAllGithubData();
        if(!RpcResultCode.SUCCESS.equals(allGithubData.getCode())){
            return R.Failed();
        }
        return R.Success(allGithubData.getData());
    }
}
