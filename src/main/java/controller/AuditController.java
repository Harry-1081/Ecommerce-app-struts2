package controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;

import com.opensymphony.xwork2.ActionContext;

import model.Audit;
import service.DatabaseService;

public class AuditController implements SessionAware
{
    String id = "-1";
    String status = "";
    String action = "";
    String endDate = "";
    String startDate = "";

    Map<String, Object> session;
    DatabaseService ds = new DatabaseService();

    public HttpHeaders index() throws ClassNotFoundException, SQLException {
        HttpServletRequest request = ServletActionContext.getRequest();
        boolean defCall = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        String role = session.get("role").toString();

        if (!defCall) {
            if(role.contains("admin"))
                return new DefaultHttpHeaders("audit").disableCaching();
            else
                return new DefaultHttpHeaders("logs").disableCaching();
        } else {
            Map<String, Object> response = new HashMap<>();
            List<Audit> logList = new LinkedList<>();
            int userId = Integer.valueOf(session.get("userId").toString());
            ResultSet res;

            if(role.contains("admin"))
            {
                if(id.equals("-1"))
                    res = ds.viewAllAudits(role);
                else
                    res = ds.viewUserAudits(":"+id,startDate,endDate,action,status,role);
            }
            else
            {
                String data = "user".equals(role) ? "UserId:"+userId : role.substring(1)+":"+userId;
                res = ds.viewUserAudits(data,startDate,endDate,action,status,role);
            }

            while(res.next()){
                int id = res.getInt("auditId");
                String time = res.getString("time");
                String action = res.getString("action");
                String status = res.getString("status");
                String doneBy = res.getString("doneBy");
                String parameter = res.getString("parameter");
                logList.add(new Audit(id, time, action, status, doneBy, parameter));
            }

            response.put("logList", logList);
            response.put("role", role);
            ActionContext.getContext().put("jsonResponse", response);
            return new DefaultHttpHeaders("success").disableCaching();
        }
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
}