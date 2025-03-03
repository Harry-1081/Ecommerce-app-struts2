package service;

import org.apache.kafka.clients.producer.*;
import java.util.Properties;

public class KafkaProducerService {

    private KafkaConsumerService kcs = new KafkaConsumerService();
    private static final String TOPIC = "inventory-updates";
    private static Producer<String, String> producer;

    static {
        Properties props = new Properties();    
        props.put("bootstrap.servers", System.getProperty("kafka.bootstrap.servers", "localhost:9092"));
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(props);
    }

    public void sendMessage(String message) {
        producer.send(new ProducerRecord<>(TOPIC, message), (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Message sent to Kafka: " + message);
            } else {
                System.err.println("Failed to send message: " + exception.getMessage());
            }
        });
        kcs.checkAlerts();
        return;
    }
    
}
