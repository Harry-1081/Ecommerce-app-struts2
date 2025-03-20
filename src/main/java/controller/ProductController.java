package controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import model.Product;
import service.DatabaseService;
import service.RedisService;

public class ProductController extends ActionSupport
{
    int id;
    private RedisService rs = new RedisService();
    private DatabaseService ds = new DatabaseService();
    Product product = new Product(0, SUCCESS, 0, 0, SUCCESS);
      
    public HttpHeaders index() throws ClassNotFoundException, SQLException {
        HttpServletRequest request = ServletActionContext.getRequest();
        boolean defCall = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        if (!defCall) {
            return new DefaultHttpHeaders("home").disableCaching();
        } else {
            Map<String, Object> response = new HashMap<>();
            List<Product> productList = new LinkedList<>();

            String result = rs.getData("productList");

            if(result!=null){
                String products[] = result.split("\n");
                for(String s:products){
                    if(s.equals(""))
                        continue;
                    productList.add(new Product(Integer.valueOf(s.split("\\|")[0]),s.split("\\|")[1],
                    Integer.valueOf(s.split("\\|")[2]),Float.valueOf(s.split("\\|")[3]),s.split("\\|")[4]));
                }
            }

            response.put("productList", productList);
            ActionContext.getContext().put("jsonResponse", response);
            return new DefaultHttpHeaders("success").disableCaching();
        }
    }
    
    public HttpHeaders show() throws ClassNotFoundException, SQLException {
        List<Product> productList = new LinkedList<>();
        Map<String, Object> response = new HashMap<>();
        ResultSet res = ds.checkAvailability(id);
        if(res.next()){
            int productId = res.getInt("productId");
            String productName = res.getString("productName");
            int quantity = res.getInt("quantity");
            float price = res.getFloat("price");
            String productImage = res.getString("productImage");
            productList.add(new Product(productId, productName, quantity, price, productImage));
        }
        response.put("productList", productList);
        response.put("message", id);
        ActionContext.getContext().put("jsonResponse", response);
        return new DefaultHttpHeaders("success").disableCaching();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
}
