package controller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import service.DailyTaskScheduler;
import service.RedisService;

public class StartupController implements ServletContextListener {

    private RedisService rs = new RedisService();
    private DailyTaskScheduler dts = new DailyTaskScheduler();

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            dts.main();
            rs.setAlertCache();
            rs.setProductCache();
            rs.setUserCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void contextDestroyed(ServletContextEvent event) {}
}