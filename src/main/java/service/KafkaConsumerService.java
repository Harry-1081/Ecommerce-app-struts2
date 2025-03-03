package service;

import org.apache.kafka.clients.consumer.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.Product;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerService {

    private static DatabaseService ds = new DatabaseService();
    private final String TOPIC = "inventory-updates";
    private final String GROUP_ID = "inventory-group";
    private final ObjectMapper mapper = new ObjectMapper();
    
    public void makeConnection()
    {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", GROUP_ID);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(TOPIC));
        
            if(true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) 
                {
                    System.out.println(record.value());
    
                    try {
                        Product product = mapper.readValue(record.value(), Product.class);
                        if (product.getQuantity() == 0) {
                            sendAlert(product);
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                consumer.commitSync(); 
            }
        }
    }
    
    private void sendAlert(Product product) throws ClassNotFoundException 
    {
        String message = "Product-" + product.getProductId() + " is out of stock!";
        System.out.println(message);
        try {
            ds.addAlert(message);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void checkAlerts(){
        makeConnection();
    }
}
