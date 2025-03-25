package controller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import service.TaskScheduler;
import service.RedisService;

public class StartupController implements ServletContextListener {

    private RedisService rs = new RedisService();
    private TaskScheduler ts = new TaskScheduler();

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            ts.setDailyTask();
            rs.setAlertCache();
            rs.setProductCache();
            rs.setUserCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void contextDestroyed(ServletContextEvent event) {}
}