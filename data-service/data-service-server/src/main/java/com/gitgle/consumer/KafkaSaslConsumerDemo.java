package com.gitgle.consumer;

import com.gitgle.config.CKafkaConfigurer;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class KafkaSaslConsumerDemo {

   public static void main(String[] args) {
      //设置JAAS配置文件的路径。
      CKafkaConfigurer.configureSaslPlain();

      //加载kafka.properties。
      Properties kafkaProperties = CKafkaConfigurer.getCKafkaProperties();

      Properties props = new Properties();
      //设置接入点，请通过控制台获取对应Topic的接入点。
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
              kafkaProperties.getProperty("bootstrap.servers"));

      //
      //  SASL_PLAINTEXT 公网接入
      //
      props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
      //  SASL 采用 Plain 方式。
      props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");

      //消费者超时时长
      //消费者超过该值没有返回心跳，服务端判断消费者处于非存活状态，服务端将消费者从Consumer Group移除并触发Rebalance，默认30s
      props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
      //两次poll的最长时间间隔
      //0.10.1.0 版本前这2个概念是混合的，都用session.timeout.ms表示
      props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 30000);
      //每次 Poll 的最大数量。
      //注意该值不要改得太大，如果 Poll 太多数据，而不能在下次 Poll 之前消费完，则会触发一次负载均衡，产生卡顿。
      props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 30);
      //消息的反序列化方式。
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
              "org.apache.kafka.common.serialization.StringDeserializer");
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
              "org.apache.kafka.common.serialization.StringDeserializer");
      //当前消费实例所属的消费组，请在控制台申请之后填写。
      //属于同一个组的消费实例，会负载消费消息。
      props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getProperty("group.id"));
      //构造消费对象，也即生成一个消费实例。
      KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
      //设置消费组订阅的 Topic，可以订阅多个。
      //如果 GROUP_ID_CONFIG 是一样，则订阅的 Topic 也建议设置成一样。
      List<String> subscribedTopics = new ArrayList<String>();
      //如果需要订阅多个 Topic，则在这里添加进去即可。
      //每个 Topic 需要先在控制台进行创建。
      String topicStr = kafkaProperties.getProperty("topic");
      String[] topics = topicStr.split(",");
      for (String topic : topics) {
         subscribedTopics.add(topic.trim());
      }
      consumer.subscribe(subscribedTopics);

      //循环消费消息。
      while (true) {
         try {
            ConsumerRecords<String, String> records = consumer.poll(1000);
            //必须在下次 Poll 之前消费完这些数据, 且总耗时不得超过 SESSION_TIMEOUT_MS_CONFIG。
            for (ConsumerRecord<String, String> record : records) {
               System.out.println(
                       String.format("Consume partition:%d offset:%d", record.partition(),
                               record.offset()));
            }
         } catch (Exception e) {
            System.out.println("consumer error!");
         }
      }
   }
}