package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

import model.Cart;
import service.DatabaseService;

public class PurchaseController extends ActionSupport implements ModelDriven<Cart>,SessionAware
{
    int id;
    Map<String, Object> session;
    DatabaseService ds = new DatabaseService();
    Cart cart = new Cart(0, 0, 0, 0, SUCCESS, SUCCESS);

    public HttpHeaders update() throws IOException {
        Map<String, Object> response = new HashMap<>();
        String message = "";
        String jsonPayload = ServletActionContext.getRequest().getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            int quantity = objectMapper.readTree(jsonPayload).get("quantity").asInt();
            int userId = Integer.valueOf(session.get("userId").toString());
            message = ds.buyProduct(id, quantity, userId, "single");
        } catch (IOException | SQLException | ClassNotFoundException e){
            message = "Error buying product";
        }
        response.put("message", message);
        ActionContext.getContext().put("jsonResponse", response);
        return new DefaultHttpHeaders("success").disableCaching();
    }

    public HttpHeaders destroy() {
        Map<String, Object> response = new HashMap<>();
        String message = "";
        int userId = Integer.valueOf(session.get("userId").toString());
        try{
            message = ds.purchaseCart(userId);
        } catch ( SQLException | ClassNotFoundException e){
            message = "Error purchasing cart";
        }
        response.put("message", message);
        ActionContext.getContext().put("jsonResponse", response);
        return new DefaultHttpHeaders("success").disableCaching();
    }
    
    @Override
    public Cart getModel() {
        return cart;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String execute() throws IOException {
        String httpMethod = ServletActionContext.getRequest().getMethod();
        if ("DELETE".equalsIgnoreCase(httpMethod)) {
            return destroy().getResultCode();  
        }
    return "SUCCESS";
    }
}
