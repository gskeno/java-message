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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * https://kafka.apache.org/37/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html
 * 发送事务消息
 */
public class TrxProducer1 {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("transactional.id", "my-transactional-id");

        Producer<String, String> producer = new KafkaProducer<>(props, new StringSerializer(), new StringSerializer());
        producer.initTransactions();
        try {
            producer.beginTransaction();
            System.out.println("beginTransaction " + new Date());
            Thread.sleep(4000);
            Future<RecordMetadata> send = producer.send(new ProducerRecord<>("trx1", "1", "1"));
            RecordMetadata recordMetadata = send.get();
            System.out.println("sendMessage 1 " + new Date() + "," + recordMetadata.offset());

            Thread.sleep(7000);
            send = producer.send(new ProducerRecord<>("trx1", "2", "2"));
            /**
             * Caused by: java.util.concurrent.ExecutionException: org.apache.kafka.common.errors.InvalidProducerEpochException: Producer attempted to produce with an old epoch.
             * 	at org.apache.kafka.clients.producer.internals.FutureRecordMetadata.valueOrError(FutureRecordMetadata.java:97)
             * 	at org.apache.kafka.clients.producer.internals.FutureRecordMetadata.get(FutureRecordMetadata.java:65)
             * 	at org.apache.kafka.clients.producer.internals.FutureRecordMetadata.get(FutureRecordMetadata.java:30)
             * 	at org.gskeno.kafka.example.transaction.TrxProducer1.main(TrxProducer1.java:40)
             */
            recordMetadata = send.get();
            System.out.println("sendMessage 2 " + new Date() + "," + recordMetadata.offset());

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
