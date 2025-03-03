package interceptor;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.ActionContext;
import java.util.Map;

public class RoleInterceptor extends AbstractInterceptor {

    private String allowedRole;
    
    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        Map<String, Object> session = ActionContext.getContext().getSession();
        String userRole = (String) session.get("role");
        if (userRole == null)
            return "noUser";
        if(allowedRole.equalsIgnoreCase("user"))
            return invocation.invoke();
            if (!userRole.equalsIgnoreCase(allowedRole))
            return "unauthorized";
            
            return invocation.invoke();
    }
    
    public void setAllowedRole(String allowedRole) {
        this.allowedRole = allowedRole;
    }
}
