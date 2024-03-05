package org.gskeno.kafka.example.transaction;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

/**
 *
 */
public class TrxConsumerUsage {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "localhost:9092");
        props.setProperty("group.id", "test");
        props.setProperty("enable.auto.commit", "false");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        // 提交的事务，其消息才可见
        props.setProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("trx1", "trx2"));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                // 打印时间，以证明commit时，才读到消息
                System.out.println(record.key() + "," + new Date() + "," + Thread.currentThread().getName());
            }
            consumer.commitSync();
        }
    }
}
