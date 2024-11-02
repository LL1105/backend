package com.gitgle.consumer.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.consumer.KafkaConsumer;
import com.gitgle.dto.NationDto;
import com.gitgle.produce.KafkaProducer;
import com.gitgle.response.GithubUser;
import com.gitgle.response.NationResponse;
import com.gitgle.result.RpcResult;
import com.gitgle.service.NationCalculationService;
import com.gitgle.service.NationService;
import com.gitgle.service.impl.NationServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@Order(2)
public class NationConsumer implements KafkaConsumer {

    private static final String TOPIC = "Nation";

    private static final String GROUP_ID = "Nation";

    private static final String USER_NATION_TOPIC = "UserNation";

    private static final Integer NATION_RETRY_COUNT = 3;

    @Resource
    private KafkaProducer kafkaProducer;

    @Resource
    private NationCalculationService nationCalculationService;

    @Resource
    private ThreadPoolTaskExecutor nationThreadPool;

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
        nationThreadPool.submit(()->{
            for (int i = 0; i < NATION_RETRY_COUNT; i++) {
                NationResponse nationResponse = nationCalculationService.calculateNation(message);
                if (ObjectUtils.isNotEmpty(nationResponse) && StringUtils.isNotEmpty(nationResponse.getNation())) {
                    NationDto nationDto = new NationDto();
                    nationDto.setNation(nationResponse.getNation());
                    nationDto.setLogin(message);
                    nationDto.setNationEnglish(nationResponse.getNationEnglish());
                    nationDto.setConfidence(nationResponse.getConfidence());
                    kafkaProducer.sendMessage(JSON.toJSONString(nationDto), USER_NATION_TOPIC);
                    break;
                }
            }
        });
    }
}
