package controller;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
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

import model.Account;
import service.DailyTaskScheduler;
import service.DatabaseService;

public class LoginController extends ActionSupport implements SessionAware 
{
    String currentRole = "";
    Map<String, Object> session;
    private DatabaseService ds = new DatabaseService();
    private DailyTaskScheduler dts = new DailyTaskScheduler();
    private Account account = new Account(0, SUCCESS, SUCCESS, SUCCESS, 0, SUCCESS);

    public HttpHeaders index() {
        HttpServletRequest request = ServletActionContext.getRequest();
        boolean defCall = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        dts.main();

        if (!defCall) {
            session.clear();
            return new DefaultHttpHeaders("login").disableCaching();
        } else {
            session.put("role", currentRole);
            return new DefaultHttpHeaders("success").disableCaching();
        }
    }

    public HttpHeaders create() throws IOException, ClassNotFoundException, SQLException {
        String message = "";
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

            if(role==null)
                role="user";

            if(account.getPassword().equals(password)){
                session.put("userId", userId);
                session.put("role", role);
                message = role;
            }
            else
                message = "Error : Your password is incorrect. Please Try again";
        }
        else
            message = "Error : The Account does not exist";
        
        response.put("message", message);
        ActionContext.getContext().put("jsonResponse", response);
        return new DefaultHttpHeaders("success").disableCaching();
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(String currentRole) {
        this.currentRole = currentRole;
    }
    
}
