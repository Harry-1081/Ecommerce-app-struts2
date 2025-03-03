package controller;

import java.io.IOException;
import java.sql.ResultSet;
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

import model.Account;
import service.DatabaseService;

public class LoginController extends ActionSupport implements SessionAware 
{
    private String message;
    Map<String, Object> session;
    private DatabaseService ds = new DatabaseService();
    private Account account = new Account(0, SUCCESS, SUCCESS, SUCCESS, 0, SUCCESS);

    public HttpHeaders index() {
        session.clear();
        return new DefaultHttpHeaders("login").disableCaching();
    }

    public HttpHeaders create() throws IOException, ClassNotFoundException, SQLException {
        Map<String, Object> response = new HashMap<>();

        String jsonPayload = ServletActionContext.getRequest().getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        ObjectMapper mapper=new ObjectMapper();
        try {
            account=mapper.readValue(jsonPayload,Account.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(ds.isAccountExists(account.getEmail())){

            ResultSet res = ds.verifyLogin(account.getEmail());

            String role = "", password = "";
            int userId = 0;

            if(res.next()){
                userId = res.getInt("id");
                password = res.getString("password");
                role = res.getString("role");
            }

            if(account.getPassword().equals(password)){
                session.put("userId", userId);
                session.put("role", role);
                message = role;
            }
            else
                message = "Your password is incorrect. Please Try again";
        }
        else
            message = "The Account does not exist";
        
        response.put("message", message);
        ActionContext.getContext().put("jsonResponse", response);
        return new DefaultHttpHeaders("success").disableCaching();
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }
    
}
