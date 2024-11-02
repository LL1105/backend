package com.gitgle.consumer.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.gitgle.constant.RedisConstant;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.consumer.KafkaConsumer;
import com.gitgle.consumer.message.DomainMessage;
import com.gitgle.entity.Domain;
import com.gitgle.entity.GithubUser;
import com.gitgle.entity.UserDomain;
import com.gitgle.mapper.DomainMapper;
import com.gitgle.mapper.GithubUserMapper;
import com.gitgle.mapper.UserDomainMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
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
@Order(3)
public class UserDomainConsumer implements KafkaConsumer {

    private static final String TOPIC = "UserDomain";

    private static final String GROUP_ID = "UserDomain";

    @Resource
    UserDomainMapper userDomainMapper;

    @Resource
    DomainMapper DomainMapper;

    @Autowired
    private DomainMapper domainMapper;

    @Resource
    RedissonClient redissonClient;

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

    public void processMessage(String message) {
        log.info("UserDomainMessage::{}", message);
        List<DomainMessage> domainMessages = JSON.parseArray(message, DomainMessage.class);
        String login = "";
        if(!domainMessages.isEmpty()) {
            login = domainMessages.get(0).getLogin();
        }

        RLock lock = redissonClient.getLock(RedisConstant.REFRESH_DOMAIN_LOCK + login);
        try {
            lock.lock(5, TimeUnit.SECONDS);
            QueryWrapper<GithubUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("login", login);
            GithubUser githubUser = githubUserMapper.selectOne(queryWrapper);
            //表中不存在则新增
            if(ObjectUtils.isEmpty(githubUser)) {
                GithubUser user = new GithubUser();
                user.setLogin(login);
                //填充头像
                RpcResult<com.gitgle.response.GithubUser> userByLogin = githubUserService.getUserByLogin(login);
                if(userByLogin.getCode().equals(RpcResultCode.SUCCESS)) {
                    user.setAvatar(userByLogin.getData().getAvatarUrl());
                }
                githubUserMapper.insert(user);
            }
        } catch (Exception e) {
            log.error("Refresh domain error: {}", e.getMessage());
        } finally {
            lock.unlock();
        }

        for (DomainMessage domainMessage : domainMessages) {
            List<UserDomain> userDomains = userDomainMapper.selectList(Wrappers.lambdaQuery(UserDomain.class).select(UserDomain::getId).eq(UserDomain::getDomain, domainMessage.getDomain()).eq(UserDomain::getLogin, domainMessage.getLogin()));
            if(ObjectUtils.isNotEmpty(userDomains)){
                userDomainMapper.deleteBatchIds(userDomains);
            }
            UserDomain userDomain = new UserDomain();
            userDomain.setDomainId(domainMessage.getDomainId());
            userDomain.setDomain(domainMessage.getDomain());
            userDomain.setConfidence(Double.valueOf(domainMessage.getConfidence().toString()));
            userDomain.setLogin(domainMessage.getLogin());
            userDomainMapper.insert(userDomain);
        }


    }
}
