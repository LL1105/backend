package com.gitgle.data.controller;

import com.gitgle.constant.RpcResultCode;
import com.gitgle.response.*;
import com.gitgle.result.Result;
import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubDataService;
import com.gitgle.service.GithubRepoService;
import com.gitgle.service.RpcDomainService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data")
public class DataController {

    @DubboReference
    private GithubDataService githubDataService;

    @DubboReference
    private RpcDomainService rpcDomainService;

    @DubboReference
    private GithubRepoService githubRepoService;

    @GetMapping("/all")
    public Result<GithubDataResponse> getAllGithubData(){
        RpcResult<GithubDataResponse> allGithubData = githubDataService.getAllGithubData();
        if(!RpcResultCode.SUCCESS.equals(allGithubData.getCode())){
            return Result.Failed();
        }
        return Result.Success(allGithubData.getData());
    }

    @GetMapping("/hot/domain")
    public Result<HotDomainResponse> getHotDomain(){
        RpcResult<HotDomainResponse> hotDomain = rpcDomainService.getHotDomain();
        if(!RpcResultCode.SUCCESS.equals(hotDomain.getCode())){
            return Result.Failed();
        }
        return Result.Success(hotDomain.getData());
    }

    @GetMapping("/hot/repo")
    public Result<GithubReposResponse> getHotRepo(){
        RpcResult<GithubReposResponse> hotRepos = githubRepoService.getHotRepos();
        if(!RpcResultCode.SUCCESS.equals(hotRepos.getCode())){
            return Result.Failed();
        }
        return Result.Success(hotRepos.getData());
    }

    @GetMapping("/hot/domain/event")
    public Result<HotDomainResponse> getHotDomainEvent(String domain){
        RpcResult<HotDomainEventResponse> hotDomainEvent = rpcDomainService.getHotDomainEvent(domain);
        if(!RpcResultCode.SUCCESS.equals(hotDomainEvent.getCode())){
            return Result.Failed();
        }
        return Result.Success(hotDomainEvent.getData());
    }
}
