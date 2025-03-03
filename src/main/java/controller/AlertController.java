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

import model.Alerts;
import service.DatabaseService;

public class AlertController extends ActionSupport
{
    Map<String, Object> session;
    DatabaseService ds = new DatabaseService();

    public HttpHeaders index() throws ClassNotFoundException, SQLException {
        HttpServletRequest request = ServletActionContext.getRequest();
        boolean defCall = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        if (!defCall) {
            return new DefaultHttpHeaders("alert").disableCaching();
        } else {
            Map<String, Object> response = new HashMap<>();
            List<Alerts> alertList = new LinkedList<>();
            ResultSet res = ds.viewAllAlerts();
            while(res.next()){
                int id = res.getInt("alertId");
                String message = res.getString("alertMessage");
                String date = res.getString("alertDate");
                alertList.add(new Alerts(id, message, date));
            }
            response.put("alertList", alertList);
            ActionContext.getContext().put("jsonResponse", response);
            return new DefaultHttpHeaders("success").disableCaching();
        }
    }

}
