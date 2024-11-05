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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public Result<GithubRepoRankResponse> getHotRepo(){
        RpcResult<GithubRepoRankResponse> hotRepos = githubRepoService.getHotRepos();
        if(!RpcResultCode.SUCCESS.equals(hotRepos.getCode())){
            return Result.Failed();
        }
        return Result.Success(hotRepos.getData());
    }

    @GetMapping("/repo/list")
    public Result<PageRepoResponse> getRepos(Integer page, Integer size){
        RpcResult<PageRepoResponse> repos = githubRepoService.getReposOrderByStar(page, size);
        if(!RpcResultCode.SUCCESS.equals(repos.getCode())){
            return Result.Failed();
        }
        return Result.Success(repos.getData());
    }

    @GetMapping("/repo")
    public Result<GithubRepos> getRepoById(String repoOwner, String repoName){
        RpcResult<GithubRepos> repoById = githubRepoService.getRepoByOwnerAndRepoName(repoOwner, repoName);
        if(!RpcResultCode.SUCCESS.equals(repoById.getCode())){
            return Result.Failed();
        }
        return Result.Success(repoById.getData());
    }
}
