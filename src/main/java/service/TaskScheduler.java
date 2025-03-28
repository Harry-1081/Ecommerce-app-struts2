package service;
import java.io.IOException;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskScheduler {
    
    private sendUserEmail sue = new sendUserEmail();
    private DatabaseService dbs = new DatabaseService();
    private static final ZoneId TIME_ZONE = ZoneId.of("Asia/Kolkata");
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final AtomicBoolean isScheduled = new AtomicBoolean(false); 
    
    public void setDailyTask() {
        if (isScheduled.compareAndSet(false, true)) {
            scheduleDailyTask(() -> {
                try {
                    dbs.generateReport();
                    sue.send();
                    System.out.println("Executed task at: " + ZonedDateTime.now(TIME_ZONE));
                } catch (ClassNotFoundException | SQLException | IOException e) {
                    System.out.print(e.toString());
                }
            });
        } else {}
    }
    
    public void scheduleDailyTask(Runnable task) {
        long initialDelay = computeNextRunDelay();
        System.out.println("Scheduling task at: " + ZonedDateTime.now(TIME_ZONE));
        scheduler.scheduleAtFixedRate(task, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

    private long computeNextRunDelay() {
        ZonedDateTime now = ZonedDateTime.now(TIME_ZONE);
        ZonedDateTime nextRun = now.withHour(0).withMinute(0).withSecond(0).withNano(0);

        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }

        return Duration.between(now, nextRun).getSeconds();
    }

        
    public void setDiscount(int productId, int quantity, float price, String dateTime) {
        scheduleDiscount(() -> {
            try {
                dbs.updateProduct(productId, quantity, price, "-");
                System.out.println("Executed task at: " + ZonedDateTime.now(TIME_ZONE));
            } catch (SQLException e) {
                System.out.print(e.toString());
            }
        }, dateTime);
    }

    public void scheduleDiscount(Runnable task,String dateTime) {
        long initialDelay = computeNextRunDelay(dateTime);
        scheduler.scheduleAtFixedRate(task, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }
        
    private long computeNextRunDelay(String dateTime) {
        ZonedDateTime now = ZonedDateTime.now(TIME_ZONE);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime starDateTime = LocalDateTime.parse(dateTime, formatter);
        ZonedDateTime nextRun = ZonedDateTime.of(starDateTime, now.getZone());
        if (now.isAfter(nextRun)) {
            nextRun = now;
        }
        return Duration.between(now, nextRun).getSeconds();
    }
}