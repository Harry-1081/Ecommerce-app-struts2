package service;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.Account;
import model.Cart;
import model.Product;
import model.Transaction;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerService {
    
    private static DatabaseService ds = new DatabaseService();
    private final String TOPIC = "inventory-updates";
    private final String GROUP_ID = "inventory-group";
    private final ObjectMapper mapper = new ObjectMapper();
    
    public void startProductConsumers(String info) {
        final int numberOfPartitions = 4;

        for (int i = 0; i < numberOfPartitions; i++) {
            final int partition = i;
            new Thread(() -> consumeFromPartition(partition,info)).start();
        }
    }

    public void consumeFromPartition(int partition,String info) {
        Properties props = new Properties();    
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", GROUP_ID);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "latest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.assign(Collections.singletonList(new TopicPartition(TOPIC, partition)));

            if (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                if (records.isEmpty()) {
                    System.out.println("No records for partition " + partition);
                }

                for (ConsumerRecord<String, String> record : records) {
                    try {
                        Product product = mapper.readValue(record.value(), Product.class);
                        if (product.getQuantity() == 0) {
                            sendAlert(product);
                        }
                        ds.addAudit("Inventry change", "Updated",info,( "ProductId:"+product.getProductId()+", Quantity:"+product.getQuantity()));
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + e.getMessage());
                    }
                }
                consumer.commitSync();
            }
        } catch (Exception e) {
            System.err.println("Error in consumer for partition " + partition + ": " + e.getMessage());
        }
    }
    
    private void sendAlert(Product product) throws ClassNotFoundException {
        String message = "Product-" + product.getProductId() + " is out of stock!";
        System.out.println(message);
        try {
            ds.addAlert(message);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void startAccountConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "account-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("account-updates"));
        
            if(true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) 
                {
                    System.out.println(record.value());
    
                    try {
                        Account account = mapper.readValue(record.value(), Account.class);
                        ds.addAudit("user signup", "created", "-", "email:"+account.getEmail());
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                consumer.commitSync(); 
            }
        }
    }
    
    public void startProductPurchaseConsumer(int userId) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "prod-purchase-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
    
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("prod-purchase-updates"));
        
            if(true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) 
                {
                    System.out.println(record.value());
    
                    try {
                        Product product = mapper.readValue(record.value(), Product.class);
                        ds.addAudit("Purchased Product", "Success", "UserId:"+userId, "ProductId:"+product.getProductId()+", Quantity:"+product.getQuantity());
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                consumer.commitSync(); 
            }
        }
    }
    
    public void startWalletBalanceConsumers() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "wallet-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
    
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("wallet-updates"));
        
            if(true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) 
                {
                    System.out.println(record.value());
    
                    try {
                        Transaction transaction = mapper.readValue(record.value(), Transaction.class);
                        ds.addAudit("Added money to wallet", "updated", "UserId:"+transaction.getUserId(), "Money:"+transaction.getAmount());
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                consumer.commitSync(); 
            }
        }
    }
    
    public void startRoleConsumers(String action,int adminId) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "role-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
    
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("role-updates"));
        
            if(true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) 
                {    
                    try {
                        Account account = mapper.readValue(record.value(), Account.class);
                        if("Admin".equals(account.getRole())) {
                            ds.addAudit("Admin role "+action, "removed".equals(action) ? action : "updated",
                                 "Superadmin", "UserId:"+account.getId());
                        } else {
                            ds.addAudit("Manager role "+action, "removed".equals(action) ? action : "updated",
                                 "admin:"+adminId, "UserId:"+account.getId());
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
    
    public void startProductRemovalConsumers(int mid) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "product-removal-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
    
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("product-removal-updates"));
        
            if(true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) 
                {    
                    try {
                        Product product = mapper.readValue(record.value(), Product.class);
                        ds.addAudit("Product Removal", "removed", "Manager:"+mid, "ProductId:"+product.getProductId());
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                consumer.commitSync(); 
            }
        }        
    }

    public void startNewProductConsumers(int mid) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "new-product-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
    
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("new-product-updates"));
        
            if(true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) 
                {
                    System.out.println(record.value());
    
                    try {
                        Product product = mapper.readValue(record.value(), Product.class);
                        ds.addAudit("New Product creation", "created", "Manager:"+mid, "ProductName:"+product.getProductName()+", ProductPrice:"+product.getPrice());
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                consumer.commitSync(); 
            }
        }
    }
    
    public void startCartConsumers() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "cart-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
    
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("cart-updates"));
        
            if(true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) 
                {
                    System.out.println(record.value());
    
                    try {
                        Cart cart = mapper.readValue(record.value(), Cart.class);
                        ds.addAudit("Cart updated", "updated", "UserId:"+cart.getUserId(), "CartId:"+cart.getCartId()+", Quantity:"+cart.getProductQuantity());
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                consumer.commitSync(); 
            }
        }
    }

    public void startNewCartConsumers() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "new-cart-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
    
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("new-cart-updates"));
        
            if(true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) 
                {
                    System.out.println(record.value());
    
                    try {
                        Cart cart = mapper.readValue(record.value(), Cart.class);
                        ds.addAudit("Product added to Cart", "added", "UserId:"+cart.getUserId(), "ProductId:"+cart.getProductId()+", Quantity:"+cart.getProductQuantity());
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                consumer.commitSync(); 
            }
        }
    }

    public void startCartRemovalConsumers() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "cart-removal-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
    
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("cart-removal-updates"));
        
            if(true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) 
                {
                    System.out.println(record.value());
    
                    try {
                        Cart cart = mapper.readValue(record.value(), Cart.class);
                        ds.addAudit("Product removed from cart", "removed", "UserId:"+cart.getUserId(), "CartId:"+cart.getCartId());
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                consumer.commitSync(); 
            }
        }
    }

    public void startCartPurchaseConsumers() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "cart-purchase-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
    
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("cart-purchase-updates"));
        
            if(true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) 
                {
                    System.out.println(record.value());
    
                    try {
                        Transaction transaction = mapper.readValue(record.value(), Transaction.class);
                        ds.addAudit("Purchased cart", "success", "UserId:"+transaction.getUserId(), "Total:"+transaction.getAmount());
                    } catch (Exception e) {
                        System.err.println("Error processing message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                consumer.commitSync(); 
            }
        }
    }
}
