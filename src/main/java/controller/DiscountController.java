package controller;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensymphony.xwork2.ActionContext;

import service.TaskScheduler;
import service.DatabaseService;

public class DiscountController 
{
    private TaskScheduler ts = new TaskScheduler();
    private DatabaseService ds = new DatabaseService();

    public HttpHeaders index() throws ClassNotFoundException, SQLException {
        return new DefaultHttpHeaders("discount").disableCaching();
    }

    public HttpHeaders create() throws ClassNotFoundException, SQLException {
        Map<String, Object> response = new HashMap<>();
        String message = "";
        try{
            String jsonPayload = ServletActionContext.getRequest().getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            ObjectMapper objectMapper = new ObjectMapper();

            int productId = objectMapper.readTree(jsonPayload).get("productId").asInt();
            int percentage = objectMapper.readTree(jsonPayload).get("percentage").asInt();
            String fromDate = objectMapper.readTree(jsonPayload).get("fromDate").asText();
            String tillDate = objectMapper.readTree(jsonPayload).get("tillDate").asText();

            ResultSet res = ds.checkAvailability(productId);
            if(res.next()){
                float price = res.getFloat("price");
                int quantity = res.getInt("quantity");
                float newPrice = (float) (price * percentage * (0.01));
                ts.setDiscount(productId, quantity, price - newPrice, fromDate);
                ts.setDiscount(productId, quantity, price, tillDate);
                message = "Scheduled successfully";
            } else { message="The product does not exist ! Check the product Id"; }
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.put("message", message);
        ActionContext.getContext().put("jsonResponse", response);
        return new DefaultHttpHeaders("success").disableCaching();
    }
}
