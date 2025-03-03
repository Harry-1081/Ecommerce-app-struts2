package controller;

import model.Account;
import service.DatabaseService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;


public class SignupController extends ActionSupport{

    private String message;
    private DatabaseService ds = new DatabaseService();
    private Account account = new Account(0, message, SUCCESS, NONE, 0, ERROR);

    public HttpHeaders index() {
        return new DefaultHttpHeaders("signup").disableCaching();
    }

    
    public HttpHeaders create() throws IOException, ClassNotFoundException, SQLException{
        Map<String, Object> response = new HashMap<>();

        String jsonPayload = ServletActionContext.getRequest().getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        ObjectMapper mapper=new ObjectMapper();
        try {
            account=mapper.readValue(jsonPayload,Account.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(!(ds.isAccountExists(account.getEmail()))) {
            ds.createAccount(account);
            message = "Account Created";
        }
        else 
            message = "Email assosiated with another account";

        response.put("message", message);
        ActionContext.getContext().put("jsonResponse", response);
        return new DefaultHttpHeaders("success").disableCaching();
    }

    public String getMessage() {
        return message;
    }
}
