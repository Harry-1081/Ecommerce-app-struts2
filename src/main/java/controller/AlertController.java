package controller;

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
import service.RedisService;

public class AlertController extends ActionSupport
{
    Map<String, Object> session;
    private RedisService rs = new RedisService();

    public HttpHeaders index() {
        HttpServletRequest request = ServletActionContext.getRequest();
        boolean defCall = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        if (!defCall) {
            return new DefaultHttpHeaders("alert").disableCaching();
        } else {
            Map<String, Object> response = new HashMap<>();
            List<Alerts> alertList = new LinkedList<>();
            
            String result = rs.getData("alertList");

            if(result!=null){
                String alerts[] = result.split("\n");
                for(String s:alerts){
                    if(s.equals(""))
                        continue;
                    alertList.add(new Alerts(Integer.valueOf(s.split("\\|")[0]),s.split("\\|")[1],s.split("\\|")[2]));
                }
            }

            response.put("alertList", alertList);
            ActionContext.getContext().put("jsonResponse", response);
            return new DefaultHttpHeaders("success").disableCaching();
        }
    }

}