package com.gitgle.consumer;

import com.gitgle.config.CKafkaConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Component
@Order(2)
@Slf4j
public class KafkaConsumerStater implements CommandLineRunner {

    @Resource
    private CKafkaConfigurer cKafkaConfigurer;

    @Resource
    private List<KafkaConsumer> kafkaConsumers;

    @Override
    public void run(String... args) throws Exception {
        for(KafkaConsumer kafkaConsumer: kafkaConsumers){
            //如果用 -D 或者其它方式设置过，这里不再设置。
            if (null == System.getProperty("java.security.auth.login.config")) {
                //请注意将 XXX 修改为自己的路径。
                System.setProperty("java.security.auth.login.config",
                        cKafkaConfigurer.getSaslJaasConfig());
            }
            Properties props = new Properties();
            //设置接入点，请通过控制台获取对应Topic的接入点。
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    cKafkaConfigurer.getBootstrapServers());
            //
            //  SASL_PLAINTEXT 公网接入
            //
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
            //  SASL 采用 Plain 方式。
            props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");

            //消费者超时时长
            //消费者超过该值没有返回心跳，服务端判断消费者处于非存活状态，服务端将消费者从Consumer Group移除并触发Rebalance，默认30s
            props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 1000000);
            //两次poll的最长时间间隔
            //0.10.1.0 版本前这2个概念是混合的，都用session.timeout.ms表示
            props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 1000000);
            //每次 Poll 的最大数量。
            //注意该值不要改得太大，如果 Poll 太多数据，而不能在下次 Poll 之前消费完，则会触发一次负载均衡，产生卡顿。
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 30);
            //消息的反序列化方式。
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                    "org.apache.kafka.common.serialization.StringDeserializer");
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                    "org.apache.kafka.common.serialization.StringDeserializer");
            Thread thread = new Thread(() -> kafkaConsumer.consumer(props));
            thread.setDaemon(true);
            thread.start();
        }
    }
}
