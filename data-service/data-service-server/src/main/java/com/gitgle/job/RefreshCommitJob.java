package com.gitgle.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gitgle.constant.RedisConstant;
import com.gitgle.convert.GithubCommitConvert;
import com.gitgle.produce.KafkaProducer;
import com.gitgle.response.GithubCommit;
import com.gitgle.response.GithubRepoRank;
import com.gitgle.service.CommitService;
import com.gitgle.utils.GithubApiRequestUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RefreshCommitJob {

    @Resource
    private KafkaProducer kafkaProducer;

    @Resource
    private ThreadPoolTaskExecutor refreshCommitTaskExecutor;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Resource
    private CommitService commitService;

    @XxlJob("refresh-commit-job")
    public void refresh(){
        log.info("执行刷新Commit任务...");
        String jobParam = XxlJobHelper.getJobParam();
        Integer count = Integer.valueOf(jobParam);
        // 获取热门仓库缓存
        List<GithubRepoRank> githubRepoRankList = redisTemplate.opsForList().range(RedisConstant.GITHUB_REPO_RANK, 0, count);
        for(GithubRepoRank githubRepoRank : githubRepoRankList){
            refreshCommitTaskExecutor.submit(()->{
                Map<String, String> params = new HashMap<>();
                Integer page = 1;
                params.put("per_page", "100");
                while(true){
                    try{
                        params.put("page", String.valueOf(page));
                        Response response = githubApiRequestUtils.listCommit(githubRepoRank.getOwnerLogin(), githubRepoRank.getRepoName(), params);
                        if(!response.isSuccessful()){
                            log.error("刷新仓库Commit失败，RepoName:{},page:{}", githubRepoRank.getRepoName(), page);
                            page++;
                            continue;
                        }
                        JSONArray responseBody = JSON.parseArray(response.body().string());
                        for(int i=0; i<responseBody.size(); i++){
                            JSONObject item =responseBody.getJSONObject(i);
                            GithubCommit githubCommit = GithubCommitConvert.convert(item);
                            // 异步入库
                            CompletableFuture.runAsync(()->{
                                commitService.writeGithubCommit2Commit(githubCommit);
                            });
                        }
                        if(responseBody.size() < 100){
                            break;
                        }
                    }catch (Exception e){
                        log.error("刷新Commit失败：{}", e.getMessage());
                    }
                    page++;
                }
            });
        }
    }
}
