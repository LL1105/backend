package com.gitgle.produce;

import com.gitgle.config.CKafkaConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.SaslConfigs;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

@Component
@Slf4j
public class KafkaProducer implements CommandLineRunner {

    @Resource
    private CKafkaConfigurer cKafkaConfigurer;

    private org.apache.kafka.clients.producer.KafkaProducer producer;

    public void sendMessage(String message, String topic) {
        try {
            //批量获取 Future 对象可以加快速度。但注意，批量不要太大。
            List<Future<RecordMetadata>> futures = new ArrayList<>(128);
            //发送消息，并获得一个Future对象。
            ProducerRecord<String, String> kafkaMessage = new ProducerRecord<>(topic,
                    message);
            Future<RecordMetadata> metadataFuture = producer.send(kafkaMessage);
            futures.add(metadataFuture);
            producer.flush();
            for (Future<RecordMetadata> future : futures) {
                //同步获得 Future 对象的结果。
                RecordMetadata recordMetadata = future.get();
                log.info("Produce ok:{}", recordMetadata.toString());
            }
        } catch (Exception e) {
            //客户端内部重试之后，仍然发送失败，业务要应对此类错误。
            log.error("Kafka Send Message ERROR:{}",e);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        //如果用 -D 或者其它方式设置过，这里不再设置。
        if (null == System.getProperty("java.security.auth.login.config")) {
            //请注意将 XXX 修改为自己的路径。
            System.setProperty("java.security.auth.login.config",
                    cKafkaConfigurer.getSaslJaasConfig());
        }

        Properties props = new Properties();
        //设置接入点，请通过控制台获取对应 Topic 的接入点。
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                cKafkaConfigurer.getBootstrapServers());

        //
        //  SASL_PLAINTEXT 公网接入
        //
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        //  SASL 采用 Plain 方式。
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");

        //消息队列 Kafka 版消息的序列化方式。
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        //请求的最长等待时间。
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30 * 1000);
        //设置客户端内部重试次数。
        props.put(ProducerConfig.RETRIES_CONFIG, 5);
        //设置客户端内部重试间隔。
        props.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 3000);
        //ack=0   producer 将不会等待来自 broker 的确认，重试配置不会生效。注意如果被限流了后，就会被关闭连接。
        //ack=1   broker leader 将不会等待所有 broker follower 的确认，就返回 ack。
        //ack=all broker leader 将等待所有 broker follower 的确认，才返回 ack。
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        //构造 Producer 对象，注意，该对象是线程安全的，一般来说，一个进程内一个Producer对象即可。
        producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
    }
}
