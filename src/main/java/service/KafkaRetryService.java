package service;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class KafkaRetryService {

    private static final String DLQ_TOPIC = "inventory-updates-dlq";
    private static final String TOPIC = "inventory-updates";

    private static KafkaConsumer<String, String> consumer;
    private static KafkaProducer<String, String> producer;

    static {
        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers",  "localhost:9092");
        consumerProps.put("group.id", "dlq-retry-group");
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<>(consumerProps);

        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers",  "localhost:9092");
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(producerProps);
    }

    public static void main(String[] args) {
        KafkaRetryService krs = new KafkaRetryService();
        krs.retryMessage();
    }

    public void retryMessage() {
        consumer.subscribe(Collections.singletonList(DLQ_TOPIC));

        if(true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                String error_message = record.value();
                String actual_message = error_message.split(",Error:")[0];
                long timestamp = System.currentTimeMillis();

                DLQMessage dlqMessage = new DLQMessage(error_message, timestamp);

                if (dlqMessage.canRetry()) {
                    producer.send(new ProducerRecord<>(TOPIC, actual_message), (metadata, exception) -> {
                        if (exception == null) {
                            System.out.println("Successfully added message to main topic: " + error_message);
                        } else {
                            System.err.println("Error while retrying message: " + exception.getMessage());
                        }
                    });
                } else {
                    System.out.println("Error 2 : " + error_message);
                }
            }
        }
    }

    public static class DLQMessage {
        String message;
        long timestamp;
        int retryCount;

        private static final int MAX_RETRIES = 3;

        public DLQMessage(String message, long timestamp) {
            this.message = message;
            this.timestamp = timestamp;
            this.retryCount = 0;
        }

        public boolean canRetry() {
            long backoffPeriod = TimeUnit.MINUTES.toMillis(5);
            long timeElapsed = System.currentTimeMillis() - timestamp;

            if (timeElapsed < backoffPeriod && retryCount < MAX_RETRIES) {
                retryCount++;
                return true;
            }
            return false;
        }
    }
}