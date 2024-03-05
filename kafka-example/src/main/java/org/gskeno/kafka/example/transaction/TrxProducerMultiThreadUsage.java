package org.gskeno.kafka.example.transaction;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * https://kafka.apache.org/37/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html
 * 发送事务消息
 */
public class TrxProducerMultiThreadUsage {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("transactional.id", "my-transactional-id");
        Producer<String, String> producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    Future<RecordMetadata> send = producer.send(new ProducerRecord<>("trx1", "y", "y"));
                    RecordMetadata recordMetadata = send.get();
                    System.out.println("sendMessage y " + new Date() + "," + recordMetadata.offset());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        // producer是线程安全的，这里表面上看，事务内只有x,z两个消息，实际上有x,y,z三个消息，它们都在
        // 事务时间内产生
        producer.initTransactions();

        try {
            producer.beginTransaction();
            System.out.println("beginTransaction " + new Date());
            Thread.sleep(4000);
            Future<RecordMetadata> send = producer.send(new ProducerRecord<>("trx1", "x", "x"));
            RecordMetadata recordMetadata = send.get();
            System.out.println("sendMessage x " + new Date() + "," + recordMetadata.offset());

            Thread.sleep(7000);
            send = producer.send(new ProducerRecord<>("trx1", "z", "z"));
            recordMetadata = send.get();
            System.out.println("sendMessage z " + new Date() + "," + recordMetadata.offset());

            Thread.sleep(2000);
            producer.commitTransaction();
            System.out.println("commitMessage " + new Date());

        } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
            // We can't recover from these exceptions, so our only option is to close the producer and exit.
            producer.close();
        } catch (
                KafkaException e) {
            // For all other exceptions, just abort the transaction and try again.
            producer.abortTransaction();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        producer.close();
    }
}
