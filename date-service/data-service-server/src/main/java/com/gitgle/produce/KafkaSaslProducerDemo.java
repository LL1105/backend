package com.gitgle.produce;

import com.gitgle.config.CKafkaConfigurer;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.SaslConfigs;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

public class KafkaSaslProducerDemo {

   public static void main(String[] args) {
      //设置 JAAS 配置文件的路径。
      CKafkaConfigurer.configureSaslPlain();

      //加载 kafka.properties。
      Properties kafkaProperties = CKafkaConfigurer.getCKafkaProperties();

      Properties props = new Properties();
      //设置接入点，请通过控制台获取对应 Topic 的接入点。
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
              kafkaProperties.getProperty("bootstrap.servers"));

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
      KafkaProducer<String, String> producer = new KafkaProducer<>(props);

      //构造一个消息队列 Kafka 版消息。
      String topic = kafkaProperties.getProperty("topic"); //消息所属的Topic，请在控制台申请之后，填写在这里。
      String value = "this is ckafka msg value"; //消息的内容。

      try {
         //批量获取 Future 对象可以加快速度。但注意，批量不要太大。
         List<Future<RecordMetadata>> futures = new ArrayList<>(128);
         for (int i = 0; i < 100; i++) {
            //发送消息，并获得一个Future对象。
            ProducerRecord<String, String> kafkaMessage = new ProducerRecord<>(topic,
                    value + ": " + i);
            Future<RecordMetadata> metadataFuture = producer.send(kafkaMessage);
            futures.add(metadataFuture);

         }
         producer.flush();
         for (Future<RecordMetadata> future : futures) {
            //同步获得 Future 对象的结果。
            RecordMetadata recordMetadata = future.get();
            System.out.println("Produce ok:" + recordMetadata.toString());
         }
      } catch (Exception e) {
         //客户端内部重试之后，仍然发送失败，业务要应对此类错误。
         System.out.println("error occurred");
      }
   }
}