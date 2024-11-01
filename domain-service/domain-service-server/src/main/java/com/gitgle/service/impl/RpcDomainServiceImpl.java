package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.dao.Domain;
import com.gitgle.request.GithubRequest;
import com.gitgle.response.*;
import com.gitgle.result.RpcResult;
import com.gitgle.service.*;
import com.gitgle.utils.SparkApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@DubboService
@Slf4j
public class RpcDomainServiceImpl implements RpcDomainService {

    @Resource
    private SparkApiUtils sparkApiUtils;

    @Resource
    private DomainService domainService;

    @DubboReference
    private GithubRepoService githubProjectService;

    @DubboReference
    private GithubCommitService githubCommitService;

    @DubboReference
    private GithubRepoService getGithubProjectService;

    @Resource
    private DomainCalculationService domainCalculationService;

    @Override
    public RpcResult<DomainResponse> getDomainByDeveloperId(String owner) {
        RpcResult<DomainResponse> domainResponseRpcResult = new RpcResult<>();
        try {
            DomainResponse domainResponse = domainCalculationService.calculationDomain(owner);
            domainResponseRpcResult.setCode(RpcResultCode.SUCCESS);
            domainResponseRpcResult.setData(domainResponse);
            return domainResponseRpcResult;
        } catch (Exception e) {
            log.error("推测开发者领域失败：{}", e);
            domainResponseRpcResult.setCode(RpcResultCode.Github_RESPONSE_FAILED);
            return domainResponseRpcResult;
        }
    }

    @Override
    public RpcResult<HotDomainResponse> getHotDomain() {
        RpcResult<HotDomainResponse> hotDomainResponseRpcResult = new RpcResult<>();
        HotDomainResponse hotDomainResponse = new HotDomainResponse();
        List<HotDomain> hotDomainList = new ArrayList<>();
        HotDomain hotDomain = new HotDomain();
        hotDomain.setDeveloperTotal(199999);
        hotDomain.setDomain("后端开发");
        hotDomainList.add(hotDomain);
        HotDomain hotDomain2 = new HotDomain();
        hotDomain2.setDeveloperTotal(99999);
        hotDomain2.setDomain("机器学习");
        hotDomainList.add(hotDomain2);
        hotDomainResponse.setHotDomainList(hotDomainList);
        hotDomainResponseRpcResult.setData(hotDomainResponse);
        hotDomainResponseRpcResult.setCode(RpcResultCode.SUCCESS);
        return hotDomainResponseRpcResult;
    }

    @Override
    public RpcResult<HotDomainEventResponse> getHotDomainEvent(String domain) {
        return null;
    }

    /**
     * 获取仓库语言
     */
    public CompletableFuture<String> getRepoLanguages(Map<String, String> githubRepoMap) {
        return CompletableFuture.supplyAsync(()->{
            StringBuilder languages = new StringBuilder();
            languages.append("仓库的编程语言信息如下：\n");
            Map<String, Integer> repoLanguagesMap = new ConcurrentHashMap<>();
            for (Map.Entry<String, String> entry : githubRepoMap.entrySet()) {
                try {
                    RpcResult<GithubLanguagesResponse> githubLanguagesRpcResult = githubProjectService.getRepoLanguages(entry.getValue(), entry.getKey());
                    if (githubLanguagesRpcResult.getData() != null) {
                        Map<String, Integer> languagesMap = githubLanguagesRpcResult.getData().getLanguagesMap();
                        if (languagesMap != null) {
                            languagesMap.forEach((language, count) ->
                                    repoLanguagesMap.merge(language, count, Integer::sum)
                            );
                        }
                    }
                } catch (Exception e) {
                    log.error("Error fetching languages for repo {}: {}", entry.getKey(), e.getMessage());
                }
            }
            languages.append(repoLanguagesMap);
            languages.append("\n");
            return languages.toString();
        });
    }

    /**
     * 获取仓库Readme
     */
    public CompletableFuture<String> getUserReposReadme(Map<String, String> githubRepoMap){
        return CompletableFuture.supplyAsync(()->{
            StringBuilder readme = new StringBuilder();
            readme.append("仓库Readme信息如下：\n");
            for (Map.Entry<String, String> githubRepo : githubRepoMap.entrySet()) {
                GithubRequest githubRequest = new GithubRequest();
                githubRequest.setOwner(githubRepo.getValue());
                githubRequest.setPath("README.md");
                githubRequest.setRepoName(githubRepo.getKey());
                RpcResult<GithubReposContent> repoContentByPath = githubProjectService.getRepoContentByPath(githubRequest);
                if (!RpcResultCode.SUCCESS.equals(repoContentByPath.getCode())) {
                    continue;
                }
                readme.append(githubRepo.getKey() + ":");
                readme.append(repoContentByPath.getData().getContent());
                readme.append("\n");
            }
            return readme.toString();
        });
    }

    /**
     * 获取用户所有提交
     */
    public RpcResult<GithubCommitResponse> getUserAllCommit(String owner){
        RpcResult<GithubCommitResponse> githubCommitResponseRpcResult = githubCommitService.searchCommitsByDeveloperId(owner);
        if (!RpcResultCode.SUCCESS.equals(githubCommitResponseRpcResult.getCode())) {
            throw new RuntimeException("获取用户Commit失败");
        }
        return githubCommitResponseRpcResult;
    }

    /**
     * 从用户提交中提取仓库
     */
    public Map<String, String> getReposFromCommits(String owner){
        Map<String, String> githubRepoMap = new HashMap<>();
        RpcResult<GithubCommitResponse> githubCommitResponseRpcResult = getUserAllCommit(owner);
        for (GithubCommit githubCommit : githubCommitResponseRpcResult.getData().getGithubCommitList()) {
            githubRepoMap.put(githubCommit.getReposName(), githubCommit.getReposOwner());
        }
        return githubRepoMap;
    }
}
