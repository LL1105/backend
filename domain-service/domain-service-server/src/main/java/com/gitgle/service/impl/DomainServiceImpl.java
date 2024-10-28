package com.gitgle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.dao.Domain;
import com.gitgle.dao.UserDomain;
import com.gitgle.mapper.DomainMapper;
import com.gitgle.mapper.UserDomainMapper;
import com.gitgle.request.GithubRequest;
import com.gitgle.response.*;
import com.gitgle.result.RpcResult;
import com.gitgle.service.DomainService;
import com.gitgle.service.GithubCommitService;
import com.gitgle.service.GithubProjectService;
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

@DubboService
@Slf4j
public class DomainServiceImpl implements DomainService {

    @Resource
    private SparkApiUtils sparkApiUtils;

    @Resource
    private DomainMapper domainMapper;

    @Resource
    private UserDomainMapper userDomainMapper;

    @DubboReference
    private GithubProjectService githubProjectService;

    @DubboReference
    private GithubCommitService githubCommitService;

    @Override
    public RpcResult<DomainResponse> getDomainByDeveloperId(String owner) {
        RpcResult<DomainResponse> domainResponseRpcResult = new RpcResult<>();
        DomainResponse domainResponse = new DomainResponse();
        try {
            // 获取用户的所有提交
            RpcResult<GithubCommitResponse> githubCommitResponseRpcResult = githubCommitService.searchCommitsByDeveloperId(owner);
            if (!RpcResultCode.SUCCESS.equals(githubCommitResponseRpcResult.getCode())) {
                domainResponseRpcResult.setCode(githubCommitResponseRpcResult.getCode());
                return domainResponseRpcResult;
            }
            // 从commit中获取仓库
            Map<String, String> githubRepoMap = new HashMap<>();
            for (GithubCommit githubCommit : githubCommitResponseRpcResult.getData().getGithubCommitList()) {
                githubRepoMap.put(githubCommit.getReposName(), githubCommit.getReposOwner());
            }
            // 从仓库中获取README,并合并到一起
            StringBuilder readme = new StringBuilder();
            for (Map.Entry<String, String> githubRepo : githubRepoMap.entrySet()) {
                GithubRequest githubRequest = new GithubRequest();
                githubRequest.setOwner(githubRepo.getValue());
                githubRequest.setPath("README.md");
                githubRequest.setRepoName(githubRepo.getKey());
                RpcResult<GithubReposContent> repoContentByPath = githubProjectService.getRepoContentByPath(githubRequest);
                if (!RpcResultCode.SUCCESS.equals(repoContentByPath.getCode())) {
                    continue;
                }
                readme.append(repoContentByPath.getData().getContent());
            }
            String question = "根据以上信息，请你分析该信息对应的开发者的专业领域和编程语言,并从下列领域名词中选择出来，同时你需要计算每个名词的置信度，你只需要给我按格式返回选择的领域和置信度(你必须尽可能精确的预测），用|符分隔（例如：|后端开发|0.77|）：";
            List<Domain> domainList = readAllDomain();
            readme.append("\n");
            readme.append(question);
            readme.append("\n");
            for (Domain domain : domainList) {
                readme.append("|");
                readme.append(domain.getDomain());
                readme.append("|");
            }
            okhttp3.Response response = sparkApiUtils.doRequest(readme.toString());
            if (!response.isSuccessful()) {
                domainResponseRpcResult.setCode(RpcResultCode.REQUEST_SPARK_FAILED);
                return domainResponseRpcResult;
            }
            JSONObject responseBody = JSON.parseObject(response.body().string());
            Integer code = responseBody.getInteger("code");
            if (code != 0) {
                domainResponseRpcResult.setCode(RpcResultCode.REQUEST_SPARK_FAILED);
                return domainResponseRpcResult;
            }
            String content = responseBody.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            log.info("Spark Response: {}", responseBody);
            log.info("Spark Response Content: {}", content);
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
                    userDomainBase.setConfidence(domain);
                    userDomainBaseList.add(userDomainBase);
                }
            }
            domainResponse.setUserDomainBaseList(userDomainBaseList);
            // 异步写库
            CompletableFuture.runAsync(()->{
                writeUserDomain(userDomainBaseList,owner);
            }).exceptionally(ex -> {
                log.error("User Domain Write Exception: {}", ex);
                return null;
            });
            domainResponseRpcResult.setCode(RpcResultCode.SUCCESS);
            domainResponseRpcResult.setData(domainResponse);
            return domainResponseRpcResult;
        } catch (IOException e) {
            log.error("获取domain失败");
            domainResponseRpcResult.setCode(RpcResultCode.Github_RESPONSE_FAILED);
            return domainResponseRpcResult;
        }
    }

    public List<Domain> readAllDomain() {
        List<Domain> domainList = domainMapper.selectList(Wrappers.lambdaQuery(Domain.class));
        return domainList;
    }

    public void writeUserDomain(List<UserDomainBase> userDomainBaseList, String owner){
        if(userDomainBaseList.isEmpty()){
            return;
        }
        userDomainBaseList.stream().forEach(userDomainBase -> {
            UserDomain userDomain = new UserDomain();
            userDomain.setDomain(userDomainBase.getDomain());
            userDomain.setConfidence(Double.valueOf(userDomainBase.getConfidence()));
            userDomain.setLogin(owner);
            userDomainMapper.insert(userDomain);
        });
    }
}
