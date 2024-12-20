package com.gitgle.consumer.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.gitgle.constant.RedisConstant;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.consumer.KafkaConsumer;
import com.gitgle.consumer.message.NationMessage;
import com.gitgle.consumer.message.TalentRankMessage;
import com.gitgle.entity.GithubUser;
import com.gitgle.mapper.GithubUserMapper;
import com.gitgle.result.RpcResult;
import com.gitgle.service.GithubUserService;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@Order(2)
public class UserNationConsumer implements KafkaConsumer {

    private static final String TOPIC = "UserNation";

    private static final String GROUP_ID = "UserNation";

    @Resource
    private RedissonClient redissonClient;

    @Resource
    GithubUserMapper githubUserMapper;

    @DubboReference
    GithubUserService githubUserService;

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
        log.info("UserNationMessage::{}", message);
        NationMessage nationMessage = JSONObject.parseObject(message, NationMessage.class);
        String login = nationMessage.getLogin();

        RLock lock = redissonClient.getLock(RedisConstant.REFRESH_USER_LOCK + login);
        try {
            lock.lock(5, TimeUnit.SECONDS);

            //先查询存不存在
            QueryWrapper<GithubUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("login", nationMessage.getLogin());
            List<GithubUser> githubUsers = githubUserMapper.selectList(queryWrapper);

            if(ObjectUtils.isEmpty(githubUsers)) {
                //不存在则新增，并填入头像url
                GithubUser user = new GithubUser();
                user.setNation(nationMessage.getNation());
                user.setLogin(nationMessage.getLogin());
                user.setNationEnglish(nationMessage.getNationEnglish());
                user.setNationConfidence(BigDecimal.valueOf(nationMessage.getConfidence()));
                user.setLocation(nationMessage.getLocation());
                //更新头像
                RpcResult<com.gitgle.response.GithubUser> githubUserRpcResult = githubUserService.getUserByLogin(user.getLogin());
                if(githubUserRpcResult.getCode().equals(RpcResultCode.SUCCESS)) {
                    user.setAvatar(githubUserRpcResult.getData().getAvatarUrl());
                }
                githubUserMapper.insert(user);
            } else {
                //存在则直接更新
                UpdateWrapper<GithubUser> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("login", nationMessage.getLogin())
                        .set("nation", nationMessage.getNation())
                        .set("nation_confidence", nationMessage.getConfidence())
                        .set("nation_english", nationMessage.getNationEnglish())
                                .set("location", nationMessage.getLocation());
                githubUserMapper.update(null, updateWrapper);
            }

        } catch (Exception e) {
            log.error("Refresh nation error: {}", e.getMessage());
        } finally {
            lock.unlock();
        }
    }
}
