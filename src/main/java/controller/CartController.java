package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

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
import service.RedisService;

public class CartController  extends ActionSupport implements ModelDriven<Cart>, SessionAware
{
    int id;
    Map<String, Object> session;
    private RedisService rs = new RedisService();
    private DatabaseService ds = new DatabaseService();
    Cart cart = new Cart(0, 0, 0, 0, SUCCESS, NONE);

    public HttpHeaders index() throws ClassNotFoundException, SQLException {
        HttpServletRequest request = ServletActionContext.getRequest();
        boolean defCall = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        if (!defCall) { 
            return new DefaultHttpHeaders("cart").disableCaching();
        } else {
            Map<String, Object> response = new HashMap<>();
            List<Cart> cartList = new LinkedList<>();
        
            int userId = Integer.valueOf(session.get("userId").toString());

            String result = rs.getData("cartList");

            if(result!=null){
                String transactions[] = result.split("\n");
                for(String s:transactions){
                    if(s.equals(""))
                        continue;
                    cartList.add(new Cart(Integer.valueOf(s.split("\\|")[0]), userId, Integer.valueOf(s.split("\\|")[1]),
                    Integer.valueOf(s.split("\\|")[4]), s.split("\\|")[2], s.split("\\|")[3]));
                }
            }
            
            response.put("cartList", cartList);
            ActionContext.getContext().put("jsonResponse", response);
            return new DefaultHttpHeaders("success").disableCaching();
        }
    }

    public HttpHeaders create() throws IOException, ClassNotFoundException, SQLException{
        Map<String, Object> response = new HashMap<>();
        String message = "";
        String jsonPayload = ServletActionContext.getRequest().getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            int productId = objectMapper.readTree(jsonPayload).get("productId").asInt();
            int quantity = objectMapper.readTree(jsonPayload).get("quantity").asInt();
            int userId = Integer.valueOf(session.get("userId").toString());
            ds.addToCart(productId, quantity, userId);
            message = "product added to cart";
        } catch (IOException e){
            message = "Error adding product to cart";
        }
        response.put("message", message);
        ActionContext.getContext().put("jsonResponse", response);
        return new DefaultHttpHeaders("success").disableCaching();
    }

    public HttpHeaders update() throws IOException {
        Map<String, Object> response = new HashMap<>();
        String message = "";
        
        String jsonPayload = ServletActionContext.getRequest().getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        ObjectMapper objectMapper = new ObjectMapper();
        int quantity = objectMapper.readTree(jsonPayload).get("quantity").asInt();

        try {
            int userId = Integer.valueOf(session.get("userId").toString());
            ds.updateCart(id, quantity,userId);
            message = "Product updated successfully !";
        } catch (SQLException | ClassNotFoundException e) {
            message = "Error updating data";
        }
        
        response.put("message", message);
        ActionContext.getContext().put("jsonResponse", response);
        return new DefaultHttpHeaders("success").disableCaching();
    }
    
    public HttpHeaders destroy() throws IOException {
        Map<String, Object> response = new HashMap<>();
        String message = "";

        try {
            int userId = Integer.valueOf(session.get("userId").toString());
            ds.clearFromCart(id,userId);
            message = "Product deleted successfully !";
        } catch (SQLException | ClassNotFoundException e) {
            message = "Error updating data";
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
    
}
