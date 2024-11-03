package com.gitgle.job;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.gitgle.constant.RedisConstant;
import com.gitgle.dto.NationDto;
import com.gitgle.produce.KafkaProducer;
import com.gitgle.response.*;
import com.gitgle.service.ContributorService;
import com.gitgle.service.UserService;
import com.gitgle.utils.GithubApiRequestUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RefreshUserJob {

    @Resource
    private KafkaProducer kafkaProducer;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ContributorService contributorService;

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Resource
    private UserService userService;

    private static final Integer REFRESH_USES_PER_SIZE = 1000;

    @Resource
    private ThreadPoolTaskExecutor refreshUserTaskExecutor;

    @XxlJob("refreshUserByHotRepo")
    public void refreshByHotRepo() {
        String param = XxlJobHelper.getJobParam();
        Integer hotRepoCount = Integer.valueOf(param);
        List<GithubRepoRank> githubRepoRankList = redisTemplate.opsForList().range(RedisConstant.GITHUB_REPO_RANK, 0, hotRepoCount);
        for(GithubRepoRank githubRepos : githubRepoRankList){
            try{
                Map<String, String> params = new HashMap<>();
                GithubContributorResponse githubContributorResponse = githubApiRequestUtils.listRepoContributors(githubRepos.getOwnerLogin(), githubRepos.getRepoName(), params);
                CompletableFuture.runAsync(()->{
                    contributorService.writeGithubContributor2Contributor(githubContributorResponse.getGithubContributorList());
                });
                for(GithubContributor githubContributor : githubContributorResponse.getGithubContributorList()){
                    kafkaProducer.sendMessage(githubContributor.getLogin(), "Domain");
                    kafkaProducer.sendMessage(githubContributor.getLogin(), "TalentRank");
                    kafkaProducer.sendMessage(githubContributor.getLogin(), "Nation");
                }
            }catch (IOException e){
                log.error("Github Contributor Exception: {}", e.getMessage());
            }
        }
    }

    /**
     * 刷新用户信息
     */
    @XxlJob("refresh-user-job")
    public void refresh() {
        log.info("执行刷新User任务...");
        // 从Redis中获取热门用户列表
        List<Integer> githubAccountIdList = redisTemplate.opsForList().range(RedisConstant.GITHUB_ACCOUNT_ID, 0, REFRESH_USES_PER_SIZE);
        if (ObjectUtils.isEmpty(githubAccountIdList)) {
            return;
        }
        // 提交线程池刷新
        for (Integer githubAccountId : githubAccountIdList) {
            refreshUserTaskExecutor.submit(() -> {
                try {
                    // 获取最新数据
                    GithubUser githubUser = githubApiRequestUtils.getUserByAccountId(githubAccountId);
                    // 如果数据没有更新，不入库，不发消息
                    GithubUser user = userService.readGithubUser2GithubUser(githubUser.getLogin());
//                    if(Objects.isNull(user)){
                        kafkaProducer.sendMessage(githubUser.getLogin(), "Domain");
//                    }
//                    if (!githubUser.equals(user)) {
                        final GithubUser finalGithubUser = githubUser;
                        // 异步入库
                        CompletableFuture.runAsync(() -> {
                            List<GithubUser> writeGithubUser = new ArrayList<>();
                            writeGithubUser.add(finalGithubUser);
                            userService.writeGithubUser2User(writeGithubUser);
                        });
                    NationDto nationDto = new NationDto();
                    nationDto.setLogin(githubUser.getLogin());
                    nationDto.setLocation(githubUser.getLocation());
                    kafkaProducer.sendMessage(JSON.toJSONString(nationDto), "Nation");
                } catch (IOException e) {
                    log.error("Github User Exception: {}", e.getMessage());
                }
            });
        }

    }

    /**
     * 刷新热门用户accountId到Redis缓存队列
     */
    @XxlJob("refresh-account-id")
    public void refreshAccountId() {
        log.info("执行刷新热门AccountId任务...");
        String param = XxlJobHelper.getJobParam();
        Integer hotFollowersCount = Integer.valueOf(param);
        if(redisTemplate.hasKey(RedisConstant.GITHUB_ACCOUNT_ID)){
            redisTemplate.delete(RedisConstant.GITHUB_ACCOUNT_ID);
        }
        redisTemplate.expire(RedisConstant.GITHUB_ACCOUNT_ID, 7, TimeUnit.DAYS);
        Integer page = 1;
        while (true) {
            Map<String, String> searchParams = new HashMap<>();
            searchParams.put("q", "followers:>" + hotFollowersCount);
            searchParams.put("sort", "followers");
            searchParams.put("per_page", "100");
            searchParams.put("page", String.valueOf(page));
            try {
                GithubUserResponse githubUserResponse = githubApiRequestUtils.searchUsers(searchParams);
                for (GithubUser githubUser : githubUserResponse.getGithubUserList()) {
                    redisTemplate.opsForList().rightPush(RedisConstant.GITHUB_ACCOUNT_ID, githubUser.getId());
                }
                if(githubUserResponse.getGithubUserList().size() < 100){
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            page++;
        }
    }
}
