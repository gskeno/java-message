
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