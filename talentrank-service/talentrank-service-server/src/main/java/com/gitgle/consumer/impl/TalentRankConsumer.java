package com.gitgle.consumer.impl;

import com.gitgle.consumer.KafkaConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
@Slf4j
public class TalentRankConsumer implements KafkaConsumer {

    private static final String TOPIC = "test";

    private static final String GROUP_ID = "test";

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

    }
}
