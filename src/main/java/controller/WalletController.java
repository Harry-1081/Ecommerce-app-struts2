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

import model.Transaction;
import service.DatabaseService;
import service.RedisService;

public class WalletController  extends ActionSupport implements SessionAware
{
    Map<String, Object> session;
    private RedisService rs = new RedisService();
    private DatabaseService ds = new DatabaseService();
    Transaction transaction = new Transaction(0, 0, 0, NONE, ERROR);

    public HttpHeaders index() throws ClassNotFoundException, SQLException {
        HttpServletRequest request = ServletActionContext.getRequest();
        boolean defCall = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        if (!defCall) {
            return new DefaultHttpHeaders("wallet").disableCaching();
        } else {
            Map<String, Object> response = new HashMap<>();
            List<Transaction> transactionList = new LinkedList<>();
        
            int userId = Integer.valueOf(session.get("userId").toString());
            float walletBalance = ds.checkWalletBalance(userId);

            String result = rs.getData("transactionList");

            if(result!=null){
                String transactions[] = result.split("\n");
                for(String s:transactions){
                    if(s.equals(""))
                        continue;
                    transactionList.add(new Transaction(Integer.valueOf(s.split("\\|")[0]),userId,
                    Float.valueOf(s.split("\\|")[1]),s.split("\\|")[2],s.split("\\|")[3]));
                }
            }

            response.put("transactionList", transactionList);
            response.put("walletBalance", walletBalance);
            ActionContext.getContext().put("jsonResponse", response);
            return new DefaultHttpHeaders("success").disableCaching();
        }
    }

    public HttpHeaders update() throws IOException {
        Map<String, Object> response = new HashMap<>();
        String message = "";
        String jsonPayload = ServletActionContext.getRequest().getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        ObjectMapper objectMapper = new ObjectMapper();
        float money = objectMapper.readTree(jsonPayload).get("money").asInt();
        try {
            int userId = Integer.valueOf(session.get("userId").toString());
            ds.updateWalletBalance( userId, money, "Added money");
            message = "Money added to wallet successfully !";
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

    @Override
    public String execute() throws IOException {
        String httpMethod = ServletActionContext.getRequest().getMethod();
        if ("PUT".equalsIgnoreCase(httpMethod)) {
            return update().getResultCode();  
        }
    return "SUCCESS";
    }
}
