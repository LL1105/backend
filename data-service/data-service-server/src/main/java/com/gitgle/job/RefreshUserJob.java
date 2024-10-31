package com.gitgle.job;

import com.gitgle.constant.RedisConstant;
import com.gitgle.produce.KafkaProducer;
import com.gitgle.response.GithubUser;
import com.gitgle.response.GithubUserResponse;
import com.gitgle.service.UserService;
import com.gitgle.utils.GithubApiRequestUtils;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RefreshUserJob {

    @Resource
    private KafkaProducer kafkaProducer;

    @Resource
    private RedisTemplate<String, Integer> redisTemplate;

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Resource
    private UserService userService;

    private Integer startIndex = 0;

    private Integer perSize = 100;

    private Integer limitTotalPage = 100;

    /**
     * 刷新用户信息
     */
    @XxlJob("refresh-user-job")
    public void refresh() {
        log.info("执行刷新User任务...");
        for (int i = 0; i <= limitTotalPage; i++) {
            List<Integer> githubAccountIdList = redisTemplate.opsForList().range(RedisConstant.GITHUB_ACCOUNT_ID, i*perSize, (i+1)*perSize);
            for (Integer githubAccountId : githubAccountIdList) {
                try {
                    // 获取最新数据
                    GithubUser githubUser = githubApiRequestUtils.getUserByAccountId(githubAccountId);
                    // 如果数据没有更新，不入库，不发消息
                    GithubUser user = userService.readGithubUser2GithubUser(githubUser.getLogin());
                    if (githubUser.equals(user)) {
                        continue;
                    }
                    final GithubUser finalGithubUser = githubUser;
                    // 异步入库
                    CompletableFuture.runAsync(() -> {
                        List<GithubUser> writeGithubUser = new ArrayList<>();
                        writeGithubUser.add(finalGithubUser);
                        userService.writeGithubUser2User(writeGithubUser);
                    });
                    // 如果用户没有设置location，发送给Nation推测，否则发给user-service
                    if(StringUtils.isNotEmpty(githubUser.getLogin())){
                        kafkaProducer.sendMessage(githubUser.getLogin(), "Nation");
                    }else {
                        kafkaProducer.sendMessage(githubUser.getLogin(), "UserNation");
                    }
                } catch (IOException e) {
                    log.error("Github User Exception: {}", e.getMessage());
                }
            }
        }
    }

    private Integer startPage = 1;

    private Integer endPage = 100;

    /**
     * 刷新热门用户accountId到Redis缓存队列
     */
    @XxlJob("refresh-account-id")
    public void refreshAccountId() {
        log.info("执行刷新热门AccountId任务...");
        redisTemplate.delete(RedisConstant.GITHUB_ACCOUNT_ID);
        redisTemplate.expire(RedisConstant.GITHUB_ACCOUNT_ID, 7, TimeUnit.DAYS);
        for (int i = startPage; i <= endPage; i++) {
            try {
                Map<String, String> searchParams = new HashMap<>();
                searchParams.put("q", "followers:>" + 0);
                searchParams.put("sort", "followers");
                searchParams.put("per_page", "100");
                searchParams.put("page", String.valueOf(i));
                GithubUserResponse githubUserResponse = githubApiRequestUtils.searchUsers(searchParams);
                for (GithubUser githubUser : githubUserResponse.getGithubUserList()) {
                    redisTemplate.opsForList().rightPush(RedisConstant.GITHUB_ACCOUNT_ID, githubUser.getId());
                }
            } catch (IOException e) {
                log.error("Github SearchUsers Exception: {}", e.getMessage());
            }
        }
    }
}
