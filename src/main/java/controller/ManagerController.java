package controller;

import java.io.IOException;
import java.sql.ResultSet;
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

import model.Product;
import service.DatabaseService;

public class ManagerController extends ActionSupport implements SessionAware
{ 
    int id;
    Map<String, Object> session;
    DatabaseService ds = new DatabaseService();
    Product product = new Product(0, SUCCESS, 0, 0, SUCCESS);

    public HttpHeaders index() throws ClassNotFoundException, SQLException {
        HttpServletRequest request = ServletActionContext.getRequest();
        boolean defCall = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        if (!defCall) {
        return new DefaultHttpHeaders("manager").disableCaching();
        } else {
             Map<String, Object> response = new HashMap<>();
            List<Product> productList = new LinkedList<>();
            ResultSet res = ds.viewAllProducts();
            while(res.next()){
                int productId = res.getInt("productId");
                String productName = res.getString("productName");
                int quantity = res.getInt("quantity");
                float price = res.getFloat("price");
                String productImage = res.getString("productImage");
                productList.add(new Product(productId, productName, quantity, price, productImage));
            }
            response.put("productList", productList);
            ActionContext.getContext().put("jsonResponse", response);
            return new DefaultHttpHeaders("success").disableCaching();
        }
    }

    public HttpHeaders create() throws SQLException, ClassNotFoundException, IOException {
        Map<String, Object> response = new HashMap<>();
        String message = "";

        String jsonPayload = ServletActionContext.getRequest().getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        ObjectMapper mapper=new ObjectMapper();

        try {
            int mid = Integer.valueOf(session.get("userId").toString());
            product=mapper.readValue(jsonPayload,Product.class);
            ds.createProduct(product,mid);
            message = "product successfully created";
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.put("message", message);
        ActionContext.getContext().put("jsonResponse", response);
        return new DefaultHttpHeaders("success").disableCaching();
    }
    
    public HttpHeaders update() throws IOException{
        Map<String, Object> response = new HashMap<>();
        String message = "";
    
        String jsonPayload = ServletActionContext.getRequest().getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        ObjectMapper mapper=new ObjectMapper();

        try {
            int mid = Integer.valueOf(session.get("userId").toString());
            product=mapper.readValue(jsonPayload,Product.class);
            message = "Product data Updated successfully";
            ds.updateProduct(id, product.getQuantity(), product.getPrice(),"Manager:"+mid);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        response.put("message", message);
        ActionContext.getContext().put("jsonResponse", response);
        return new DefaultHttpHeaders("success").disableCaching();
    }

    public HttpHeaders destroy() throws IOException{
        Map<String, Object> response = new HashMap<>();
        String message = "";
        
        try {
            int mid = Integer.valueOf(session.get("userId").toString());
            ds.deleteProduct(id,mid);
            message = "Product deleted successfully !";
        } catch (SQLException | ClassNotFoundException e) {
            message = "Error updating data";
        }
        
        response.put("message", message);
        ActionContext.getContext().put("jsonResponse", response);
        return new DefaultHttpHeaders("success").disableCaching();
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
