package org.gskeno.kafka.example;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 单机运行
 */
public class StandaloneUsage {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Producer<String, String> producer = getProducer();

        // Kafka 主题名称
        String topic = "quickstart-events";
        for (int i = 0; i < 100; i++) {
            // 发送消息
            String message = "Hello, Kafka!" + i;
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
            Future<RecordMetadata> send = producer.send(record);
            RecordMetadata recordMetadata = send.get();
            System.out.println("message:" + message + ",result:" + recordMetadata);
            Thread.sleep(3000);
        }


        // 关闭 Kafka 生产者
        producer.close();
    }

    private static Producer<String, String> getProducer() {
        Properties properties = getProperties();
        // 创建 Kafka 生产者
        Producer<String, String> producer = new KafkaProducer<>(properties);
        return producer;
    }

    private static Properties getProperties() {
        // Kafka 服务器地址和端口
        String bootstrapServers = "localhost:9092";
        // Kafka 生产者配置
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // 默认值就是1， leader发送成功就算成功
        properties.put(ProducerConfig.ACKS_CONFIG, "1");
        // 所有副本都写成功，才算成功
        // Caused by: org.apache.kafka.common.errors.TimeoutException: Expiring 1 record(s) for quickstart-events-0:120002 ms has passed since batch creation
        //properties.put(ProducerConfig.ACKS_CONFIG, "all");

        // 生产者发送消息后，不需要等待服务端的响应
        properties.put(ProducerConfig.ACKS_CONFIG, "0");

        return properties;
    }
}
