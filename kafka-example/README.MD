# 事务消息
如果producer1在事务消息commit前send了两条消息A和C，producer2在A，C发送时间之内之间send消息B。简单来说就是普通消息B卡在了事务消息A，C之间，
那么producer1在commit之前，消费者能消费消息B吗

**消费不了，得commit后，消费者能顺序收到A，B，C消息** 

---

# 异常

## 单机发送消息时，broker服务中断，客户端异常
异常信息来自于`Sender`类
```text
Exception in thread "main" java.util.concurrent.ExecutionException: org.apache.kafka.common.errors.TimeoutException: Expiring 1 record(s) for quickstart-events-0:120001 ms has passed since batch creation
	at org.apache.kafka.clients.producer.internals.FutureRecordMetadata.valueOrError(FutureRecordMetadata.java:97)
	at org.apache.kafka.clients.producer.internals.FutureRecordMetadata.get(FutureRecordMetadata.java:65)
	at org.apache.kafka.clients.producer.internals.FutureRecordMetadata.get(FutureRecordMetadata.java:30)
	at org.gskeno.kafka.example.StandaloneUsage.main(StandaloneUsage.java:25)
Caused by: org.apache.kafka.common.errors.TimeoutException: Expiring 1 record(s) for quickstart-events-0:120001 ms has passed since batch creation
```

## 在事务执行过程中producer正在发消息，如果另外一个producer启用了相同的transaction.id，也在执行事务，则先执行的事务发生异常(epoch较小)

异常情况1
```java
Caused by: java.util.concurrent.ExecutionException: org.apache.kafka.common.errors.InvalidProducerEpochException: Producer attempted to produce with an old epoch.
	at org.apache.kafka.clients.producer.internals.FutureRecordMetadata.valueOrError(FutureRecordMetadata.java:97)
	at org.apache.kafka.clients.producer.internals.FutureRecordMetadata.get(FutureRecordMetadata.java:65)
	at org.apache.kafka.clients.producer.internals.FutureRecordMetadata.get(FutureRecordMetadata.java:30)
	at org.gskeno.kafka.example.transaction.TrxProducer1.main(TrxProducer1.java:40)
```

异常情况2,一般出现在另一个producer使用相同的transaction.id，且执行`producer.initTransactions();`后
```java
Caused by: java.util.concurrent.ExecutionException: org.apache.kafka.common.errors.ProducerFencedException: There is a newer producer with the same transactionalId which fences the current one.
	at org.apache.kafka.clients.producer.internals.FutureRecordMetadata.valueOrError(FutureRecordMetadata.java:97)
	at org.apache.kafka.clients.producer.internals.FutureRecordMetadata.get(FutureRecordMetadata.java:65)
	at org.apache.kafka.clients.producer.internals.FutureRecordMetadata.get(FutureRecordMetadata.java:30)
	at org.gskeno.kafka.example.transaction.TrxProducer2.main(TrxProducer2.java:35)
```