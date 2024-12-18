package com.gitgle.consumer.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.gitgle.constant.RedisConstant;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.consumer.KafkaConsumer;
import com.gitgle.consumer.message.TalentRankMessage;
import com.gitgle.entity.GithubUser;
import com.gitgle.mapper.GithubUserMapper;
import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubUserService;
import com.gitgle.service.UserDomainService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@Order(1)
public class UserTalentRankConsumer implements KafkaConsumer {

    private static final String TOPIC = "UserTalentRank";

    private static final String GROUP_ID = "UserTalentRank";

    @Resource
    GithubUserMapper githubUserMapper;

    @DubboReference
    GithubUserService githubUserService;

    @Resource
    RedissonClient redissonClient;

    @Resource
    private UserDomainService userDomainService;

    @Override
    public void consumer(Properties props) {
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        try (org.apache.kafka.clients.consumer.KafkaConsumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(props)) {
            List<String> subscribedTopics = Collections.singletonList(TOPIC);
            consumer.subscribe(subscribedTopics);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutdown initiated, closing consumer.");
                consumer.wakeup(); // 触发异常，跳出循环
            }));

            while (true) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                    if (records.isEmpty()) {
                        // 没有新消息时休眠一段时间
                        Thread.sleep(100); // 休眠 100 毫秒
                        continue; // 跳过本次循环，重新检查
                    }

                    for (ConsumerRecord<String, String> record : records) {
                        log.info("Consume partition:{} offset:{}", record.partition(), record.offset());
                        processMessage(record.value());
                    }
                    // 批量提交偏移量
                    consumer.commitSync();
                } catch (WakeupException e) {
                    log.info("Consumer wakeup triggered.");
                    break; // 退出循环
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 重新设置中断标志
                    log.error("Thread was interrupted: {}", e);
                } catch (Exception e) {
                    log.error("Consumer encountered an error: {}", e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to create or close consumer: {}", e);
        }
    }

    public void processMessage(String message){
        log.info("UserTalentRankMessage::{}", message);
        TalentRankMessage talentRankMessage = JSONObject.parseObject(message, TalentRankMessage.class);
        String login = talentRankMessage.getLogin();
        RLock lock = redissonClient.getLock(RedisConstant.REFRESH_USER_LOCK + login);
        try {
            lock.lock(5, TimeUnit.SECONDS);
            QueryWrapper<GithubUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("login", login);
            List<GithubUser> githubUsers = githubUserMapper.selectList(queryWrapper);
            if(ObjectUtils.isEmpty(githubUsers)) {
                //插入
                GithubUser user = new GithubUser();
                user.setTalentRank(new BigDecimal(talentRankMessage.getTalentRank()));
                user.setLogin(talentRankMessage.getLogin());
                //更新头像
                RpcResult<com.gitgle.response.GithubUser> githubUserRpcResult = githubUserService.getUserByLogin(user.getLogin());
                if(githubUserRpcResult.getCode().equals(RpcResultCode.SUCCESS)) {
                    user.setAvatar(githubUserRpcResult.getData().getAvatarUrl());
                }
                githubUserMapper.insert(user);
            } else {
                //更新Rank
                UpdateWrapper<GithubUser> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("login", talentRankMessage.getLogin()).set("talent_rank", new BigDecimal(talentRankMessage.getTalentRank()));
                githubUserMapper.update(null, updateWrapper);
                userDomainService.updateUserDomainTalentRank(talentRankMessage.getLogin(), talentRankMessage.getTalentRank());
            }
        } catch (Exception e) {
            log.error("Refresh talentRank error: {}", e.getMessage());
        } finally {
            lock.unlock();
        }
    }
}
