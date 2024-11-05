package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.dao.Domain;
import com.gitgle.request.GithubRequest;
import com.gitgle.response.*;
import com.gitgle.result.RpcResult;
import com.gitgle.service.DomainCalculationService;
import com.gitgle.service.DomainService;
import com.gitgle.service.GithubCommitService;
import com.gitgle.service.GithubRepoService;
import com.gitgle.utils.SparkApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DomainCalculationServiceImpl implements DomainCalculationService {

    @Resource
    private SparkApiUtils sparkApiUtils;

    @Resource
    private DomainService domainService;

    @DubboReference
    private GithubRepoService githubProjectService;

    @DubboReference
    private GithubCommitService githubCommitService;

    private static final Double DOMAIN_FOOTER_SCORE = 0.6;


    @Override
    public DomainResponse calculationDomain(String owner) {
        DomainResponse domainResponse = new DomainResponse();
        try {
            // 获取用户参与提交的所有仓库
            Map<String, String> githubRepoMap = getReposFromCommits(owner);
            // 从仓库中获取README,并合并到一起
            CompletableFuture<String> userReposReadme = getUserReposReadme(githubRepoMap);
            // 获取仓库语言
            CompletableFuture<String> repoLanguages = getRepoLanguages(githubRepoMap);
            CompletableFuture.allOf(userReposReadme, repoLanguages);
            StringBuilder description = new StringBuilder();
            description.append(userReposReadme.get());
            description.append(repoLanguages.get());
            String question = "根据以上信息，请你分析该信息对应的开发者的专业领域和编程语言,并从下列领域名词中选择出来(你的回答必须在下列名词中，你不能回答不包含于下列的答案)，同时你需要计算每个名词的置信度，你只需要给我按格式返回选择的领域和置信度(不要给出其他任何提示信息），用|符分隔（例如：|后端开发|0.77|Java|0.88|）：";
            List<Domain> domainList = domainService.readAllDomain();
            description.append("\n");
            description.append(question);
            description.append("\n");
            for (Domain domain : domainList) {
                description.append("|");
                description.append(domain.getDomain());
                description.append("|");
            }
            okhttp3.Response response = sparkApiUtils.doRequest(description.toString());
            JSONObject responseBody = JSON.parseObject(response.body().string());
            String content = responseBody.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            List<UserDomainBase> userDomainBaseList = new ArrayList<>();
            String[] domainArray = content.split("\\|");
            for(int i=0;i<domainArray.length;i++){
                String domain = domainArray[i];
                if("\n".equals(domain)){
                    continue;
                }
                if(domain.matches("-?\\d+(\\.\\d+)?")){
                    UserDomainBase userDomainBase = new UserDomainBase();
                    userDomainBase.setDomain(domainArray[i-1]);
                    if(Double.valueOf(domain) <= DOMAIN_FOOTER_SCORE){
                        continue;
                    }
                    userDomainBase.setConfidence(domain);
                    userDomainBaseList.add(userDomainBase);
                }
            }
            domainResponse.setUserDomainBaseList(userDomainBaseList);
            return domainResponse;
        } catch (Exception e) {
            log.error("推测开发者领域失败：{}", e.getMessage());
            return null;
        }
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
                    log.error("获取仓库语言失败，仓库：{}，语言：{}", entry.getKey(), e.getMessage());
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
