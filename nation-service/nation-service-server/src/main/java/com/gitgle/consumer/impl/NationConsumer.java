package com.gitgle.consumer.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.gitgle.constant.RpcResultCode;
import com.gitgle.consumer.KafkaConsumer;
import com.gitgle.produce.KafkaProducer;
import com.gitgle.response.GithubUser;
import com.gitgle.response.NationResponse;
import com.gitgle.result.RpcResult;
import com.gitgle.service.NationService;
import com.gitgle.service.impl.NationServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class NationConsumer implements KafkaConsumer {

    private static final String TOPIC = "Nation";

    private static final String GROUP_ID = "Nation";

    private static final String USER_NATION_TOPIC = "UserNation";

    private static final Integer NATION_RETRY_COUNT = 3;

    @Resource
    private KafkaProducer kafkaProducer;

    @Resource
    private NationService nationService;

    @Override
    public void consumer(Properties props) {
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        org.apache.kafka.clients.consumer.KafkaConsumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<String, String>(props);
        List<String> subscribedTopics = new ArrayList<String>();
        subscribedTopics.add(TOPIC);
        consumer.subscribe(subscribedTopics);
        try {
            // 循环消费消息
            while (true) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                    for (ConsumerRecord<String, String> record : records) {
                        log.info("Consume partition:{} offset:{}", record.partition(), record.offset());
                        processMessage(record.value());
                        // 消费完成后手动提交偏移量
                        consumer.commitSync();
                    }
                } catch (Exception e) {
                    log.error("Consumer encountered an error: {}", e);
                }
            }
        } finally {
            // 关闭消费者资源
            consumer.close();
        }
    }

    public void processMessage(String message){
        GithubUser githubUser = JSON.parseObject(message, GithubUser.class);
        CompletableFuture.runAsync(()->{
            NationResponse nationResponse = new NationResponse();
            nationResponse.setNation(githubUser.getLocation());
            nationResponse.setConfidence(1.0);
            if(StringUtils.isEmpty(githubUser.getLocation())){
                for(int i=0;i<NATION_RETRY_COUNT;i++){
                    RpcResult<NationResponse> nationResponseRpcResult = nationService.getNationByDeveloperId(githubUser.getLogin());
                    if(RpcResultCode.SUCCESS.equals(nationResponseRpcResult.getCode())){
                        nationResponse = nationResponseRpcResult.getData();
                        break;
                    }
                }
            }
            if(StringUtils.isNotEmpty(nationResponse.getNation())){
                kafkaProducer.sendMessage(JSON.toJSONString(nationResponse), USER_NATION_TOPIC);
            }
        });
    }
}
