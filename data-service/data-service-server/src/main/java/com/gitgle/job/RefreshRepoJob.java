package com.gitgle.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gitgle.constant.RedisConstant;
import com.gitgle.convert.GithubRepoConvert;
import com.gitgle.produce.KafkaProducer;
import com.gitgle.response.*;
import com.gitgle.service.ContributorService;
import com.gitgle.service.ReposService;
import com.gitgle.service.UserService;
import com.gitgle.utils.GithubApiRequestUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RefreshRepoJob {

    @Resource
    private KafkaProducer kafkaProducer;

    @Resource
    private ContributorService contributorService;

    @Resource
    private ReposService reposService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @XxlJob("refreshRepoByStar")
    public void refreshByStar(){
        String[] params = XxlJobHelper.getJobParam().split(",");
        Integer starsUpperLimit = 0;
        if(params.length==2){
            starsUpperLimit = Integer.valueOf(params[1]);
        }
        Integer starsFloorLimit = Integer.valueOf(params[0]);
        redisTemplate.delete(RedisConstant.GITHUB_ACCOUNT_ID);
        redisTemplate.expire(RedisConstant.GITHUB_ACCOUNT_ID, 7, TimeUnit.DAYS);
        Integer page = 1;
        while (true) {
            Map<String, String> searchParams = new HashMap<>();
            // 判断是否有stars上限
            if(!starsUpperLimit.equals(0)){
                searchParams.put("q", "stars:" + starsFloorLimit + ".." + starsUpperLimit);
            }else {
                searchParams.put("q", "stars:>=" + starsFloorLimit);
            }
            searchParams.put("sort", "stars");
            searchParams.put("per_page", "100");
            searchParams.put("page", String.valueOf(page));
            try {
                Response response = githubApiRequestUtils.searchRepos(searchParams);
                if(!response.isSuccessful()){
                    log.info("刷新仓库失败，停止刷新");
                    return;
                }
                JSONObject responseBody = JSON.parseObject(response.body().string());
                JSONArray items = responseBody.getJSONArray("items");
                for(int i=0;i<items.size();i++){
                    JSONObject item = items.getJSONObject(i);
                    GithubRepos githubRepos = GithubRepoConvert.convert(item);
                    // 如果没有上限则缓存仓库排行
                    if(starsUpperLimit.equals(0)){
                        if(redisTemplate.hasKey(RedisConstant.GITHUB_REPO_RANK) && redisTemplate.opsForList().size(RedisConstant.GITHUB_REPO_RANK) > 1000){
                            log.info("热门仓库缓存已经超过1000");
                        }else{
                            // 缓存到redis排行
                            GithubRepoRank githubRepoRank = new GithubRepoRank();
                            githubRepoRank.setRepoName(githubRepos.getRepoName());
                            githubRepoRank.setStarsCount(githubRepos.getStarsCount());
                            githubRepoRank.setOwnerLogin(githubRepos.getOwnerLogin());
                            githubRepoRank.setOwnerAvatarUrl(githubRepos.getOwnerAvatarUrl());
                            githubRepoRank.setRepoId(githubRepos.getId());
                            githubRepoRank.setWatchersCount(githubRepos.getWatchersCount());
                            githubRepoRank.setForksCount(githubRepos.getForksCount());
                            redisTemplate.opsForList().rightPush(RedisConstant.GITHUB_REPO_RANK, githubRepoRank);
                            redisTemplate.expire(RedisConstant.GITHUB_REPO_RANK, 7, TimeUnit.DAYS);
                        }
                    }
                    GithubRepos repos = reposService.readRepos2GithubRepos(githubRepos.getOwnerLogin(), githubRepos.getRepoName());
                    if(githubRepos.equals(repos)){
                        continue;
                    }
                    // 异步写库
                    CompletableFuture.runAsync(()->{
                        reposService.writeGithubRepos2Repos(githubRepos);
                    });
                    Map<String, String> param = new HashMap<>();
                    // 刷新仓库贡献者
                    GithubContributorResponse githubContributorResponse = githubApiRequestUtils.listRepoContributors(githubRepos.getOwnerLogin(), githubRepos.getRepoName(), param);
                    // 计算仓库贡献总值
                    Integer totalContributions = 0;
                    for(GithubContributor githubContributor : githubContributorResponse.getGithubContributorList()){
                        totalContributions += githubContributor.getContributions();
                    }
                    // 计算旧的仓库贡献总值
                    Integer oldTotalContributions = 0;
                    List<GithubContributor> oldGithubContributotList = contributorService.readContributor2GithubContributor(githubContributorResponse.getGithubContributorList().get(0).getRepoName(),
                            githubContributorResponse.getGithubContributorList().get(0).getRepoOwner());
                    for(GithubContributor githubContributor : oldGithubContributotList){
                        oldTotalContributions += githubContributor.getContributions();
                    }
                    // 如果新旧值不一致，则发送消息
                    if(!totalContributions.equals(oldTotalContributions)){
                        // 异步写库
                        CompletableFuture.runAsync(()->{
                            contributorService.writeGithubContributor2Contributor(githubContributorResponse.getGithubContributorList());
                        });
                        kafkaProducer.sendMessage(githubRepos.getOwnerLogin(), "TalentRank");
                        continue;
                    }
                    // 如果仓库指标不一致，则发送消息
                    if(!githubRepos.getStarsCount().equals(repos.getStarsCount()) || !githubRepos.getForksCount().equals(repos.getForksCount()) || !githubRepos.getIssueCount().equals(repos.getIssueCount())){
                        kafkaProducer.sendMessage(githubRepos.getOwnerLogin(), "TalentRank");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            page++;
        }
    }
}
