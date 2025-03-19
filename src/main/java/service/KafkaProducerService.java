package service;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.Product;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class KafkaProducerService {

    private int key;
    KafkaRetryService krs = new KafkaRetryService();
    private static final String TOPIC = "inventory-updates";
    private static final String DLQ_TOPIC = "inventory-updates-dlq";
    private static Producer<String, String> producer;

    static {
        Properties props = new Properties();    
        props.put("bootstrap.servers", System.getProperty("kafka.bootstrap.servers", "localhost:9092"));
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
    }

    public void sendProductMessage(String message) {
        ObjectMapper mapper = new ObjectMapper();
        Product product = null;
        
        try {
            product = mapper.readValue(message, Product.class);
        } catch (IOException e) {
            handleError(message, e);
            return;
        }
        
        List<PartitionInfo> partitions = producer.partitionsFor(TOPIC);
        key = ( Utils.murmur2((product.getProductId()+"").getBytes()) & 0x7FFFFFFF ) % partitions.size();

        try{
            producer.send(new ProducerRecord<String,String>(TOPIC, String.valueOf(key), message), (metadata, exception) -> {
                System.out.println("Message sent to Kafka: " + message);
            });
        } catch(Exception e){
            handleError(message, e);
        }
    }

    public void handleError(String message,Exception e) {
        String error_log = message+",Error:"+e.getMessage();
        producer.send(new ProducerRecord<String,String>(DLQ_TOPIC, error_log), (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Error Log sent to Kafka: " + error_log);
                krs.retryMessage();
            } else {
                System.err.println("Failed to send log: " + exception.getMessage());
            }
        });
    }

    public static void closeProducer() {
        if (producer != null) {
            producer.close();
        }
    }

    public void sendAccountCreationMessage(String kafkaMessage) {
        try{
            producer.send(new ProducerRecord<String,String>("account-updates", kafkaMessage), (metadata, exception) -> {
                System.out.println("Message sent to Kafka: " + kafkaMessage);
            });
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void sendProductPurchaseMessage(String kafkaMessage) {
        try{
            producer.send(new ProducerRecord<String,String>("prod-purchase-updates", kafkaMessage), (metadata, exception) -> {
                System.out.println("Message sent to Kafka: " + kafkaMessage);
            });
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void sendWalletBalanceMessage(String kafkaMessage) {
        try{
            producer.send(new ProducerRecord<String,String>("wallet-updates", kafkaMessage), (metadata, exception) -> {
                System.out.println("Message sent to Kafka: " + kafkaMessage);
            });
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void sendRoleMessage(String kafkaMessage) {
        try{
            producer.send(new ProducerRecord<String,String>("role-updates", kafkaMessage), (metadata, exception) -> {
                System.out.println("Message sent to Kafka: " + kafkaMessage);
            });
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void sendProductRemovalMessage(String kafkaMessage) {
        try{
            producer.send(new ProducerRecord<String,String>("product-removal-updates", kafkaMessage), (metadata, exception) -> {
                System.out.println("Message sent to Kafka: " + kafkaMessage);
            });
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void sendNewProductMessage(String kafkaMessage) {
        try{
            producer.send(new ProducerRecord<String,String>("new-product-updates", kafkaMessage), (metadata, exception) -> {
                System.out.println("Message sent to Kafka: " + kafkaMessage);
            });
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void sendCartMessage(String kafkaMessage) {
        try{
            producer.send(new ProducerRecord<String,String>("cart-updates", kafkaMessage), (metadata, exception) -> {
                System.out.println("Message sent to Kafka: " + kafkaMessage);
            });
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void sendNewCartMessage(String kafkaMessage) {
        try{
            producer.send(new ProducerRecord<String,String>("new-cart-updates", kafkaMessage), (metadata, exception) -> {
                System.out.println("Message sent to Kafka: " + kafkaMessage);
            });
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void sendCartRemovalMessage(String kafkaMessage) {
        try{
            producer.send(new ProducerRecord<String,String>("cart-removal-updates", kafkaMessage), (metadata, exception) -> {
                System.out.println("Message sent to Kafka: " + kafkaMessage);
            });
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void sendCartPurchaseMessage(String kafkaMessage) {
        try{
            producer.send(new ProducerRecord<String,String>("cart-purchase-updates", kafkaMessage), (metadata, exception) -> {
                System.out.println("Message sent to Kafka: " + kafkaMessage);
            });
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
}