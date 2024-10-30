package com.gitgle.job;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gitgle.produce.KafkaProducer;
import com.gitgle.response.GithubUser;
import com.gitgle.response.GithubUserResponse;
import com.gitgle.service.UserService;
import com.gitgle.utils.GithubApiRequestUtils;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
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
    private RedisTemplate redisTemplate;

    @Resource
    private GithubApiRequestUtils githubApiRequestUtils;

    @Resource
    private UserService userService;

    private static final Integer LIMIT_PAGE = 1000;

    private static final Integer CACHE_LIMIT_PAGE = 100;

    @XxlJob("refresh-user-job")
    public void refresh() {
        log.info("执行刷新User任务...");
        for (int i = 0; i <= LIMIT_PAGE; i++) {
            List<Integer> githubAccountIdList = redisTemplate.opsForList().range("github:account:id", i * LIMIT_PAGE, (i + 1) * LIMIT_PAGE);
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
//                    // 如果用户没有设置location，发送给Nation
//                    if(StringUtils.isNotEmpty(githubUser.getLogin())){
//                        kafkaProducer.sendMessage(githubUser.getLogin(), "Nation");
//                    }
//                    // 发送给user-service
//                    kafkaProducer.sendMessage(JSON.toJSONString(githubUser), "GithubUserData");
                } catch (IOException e) {
                    log.info("Github GetUserByAccountId Exception: {}", e);
                }
            }
        }
    }

    @XxlJob("refresh-account-id")
    public void refreshAccountId() {
        log.info("执行刷新热门AccountId任务...");
        redisTemplate.delete("github:account:id");
        redisTemplate.expire("github:account:id", 7, TimeUnit.DAYS);
        for (int i = 1; i <= LIMIT_PAGE; i++) {
            try {
                Map<String, String> searchParams = new HashMap<>();
                searchParams.put("q", "followers:>" + 0);
                searchParams.put("sort", "followers");
                searchParams.put("per_page", "100");
                searchParams.put("page", String.valueOf(i));
                GithubUserResponse githubUserResponse = githubApiRequestUtils.searchUsers(searchParams);
                for (GithubUser githubUser : githubUserResponse.getGithubUserList()) {
                    redisTemplate.opsForList().rightPush("github:account:id", githubUser.getId());
                }
            } catch (IOException e) {
                log.error("Github SearchUsers Exception: {}", e.getMessage());
            }
        }
    }
}
