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

import model.Account;
import service.DatabaseService;
import service.RedisService;

public class AdminController extends ActionSupport implements SessionAware
{    
    int id;
    Map<String, Object> session;
    private RedisService rs = new RedisService();
    private DatabaseService ds = new DatabaseService();
    Account account = new Account(0, NONE, NONE, SUCCESS, 0, NONE);
    
    public HttpHeaders index() throws ClassNotFoundException, SQLException {

        HttpServletRequest request = ServletActionContext.getRequest();
        boolean defCall = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        if (!defCall) {
            return new DefaultHttpHeaders("admin").disableCaching();
        } else {
            Map<String, Object> response = new HashMap<>();
            List<Account> userList = new LinkedList<>();

            String result = rs.getData("userList");

            if(result!=null){
                String products[] = result.split("\n");
                for(String s:products){
                    if(s.equals(""))
                        continue;
                    userList.add(new Account(Integer.valueOf(s.split("\\|")[0]),s.split("\\|")[1],
                    (s.split("\\|")[2]),"*",0,s.split("\\|")[3]));
                }
            }

            response.put("userList", userList);
            ActionContext.getContext().put("jsonResponse", response);
            return new DefaultHttpHeaders("success").disableCaching();
        }
    }
    
    public HttpHeaders update() throws IOException {
        Map<String, Object> response = new HashMap<>();
        String message = "";
        String jsonPayload = ServletActionContext.getRequest().getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        ObjectMapper objectMapper = new ObjectMapper();
        String type = objectMapper.readTree(jsonPayload).get("type").asText();
        try {
            int userId = Integer.valueOf(session.get("userId").toString());
            if("promote".equals(type)){
                if(!ds.addManager(id,userId))
                    message = "User cannot have two roles";
                else
                    message = "Role updated successfully !";
            }
            else if("demote".equals(type)){
                ds.removeManager(id, userId);
                message = "Role updated successfully !";
            }
        } catch (SQLException | ClassNotFoundException e) {
            message = "Error updating roles";
        }
        response.put("message", message);
        ActionContext.getContext().put("jsonResponse", response);
        return new DefaultHttpHeaders("success");
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
