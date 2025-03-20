package service;

import java.sql.ResultSet;
import java.sql.SQLException;

import redis.clients.jedis.Jedis;

public class RedisService 
{
    private static DatabaseService db = new DatabaseService();

    public void setProductCache() 
    {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            System.out.println("Connection to server");
            ResultSet res = db.viewAllProducts();
            jedis.set("productList", makeProductString(res));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String makeProductString(ResultSet res)
    {
        StringBuilder sb = new StringBuilder();
        try {
            while (res.next()) {
                sb.append(res.getInt("productId")).append("|")
                  .append(res.getString("productName")).append("|")
                  .append(res.getInt("quantity")).append("|")
                  .append(res.getFloat("price")).append("|")
                  .append(res.getString("productImage")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

    public void setUserCache() 
    {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            System.out.println("Connection to server");
            ResultSet res = db.viewAllUsers();
            jedis.set("userList", makeUserString(res));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String makeUserString(ResultSet res)
    {
        StringBuilder sb = new StringBuilder();
        try {
            while (res.next()) {
                sb.append(res.getInt("id")).append("|")
                  .append(res.getString("name")).append("|")
                  .append(res.getString("email")).append("|")
                  .append(res.getString("role")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

    
    public void setAlertCache() 
    {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            System.out.println("Connection to server");
            ResultSet res = db.viewAllAlerts();
            jedis.set("alertList", makeAlertString(res));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String makeAlertString(ResultSet res)
    {
        StringBuilder sb = new StringBuilder();
        try {
            while (res.next()) {
                sb.append(res.getInt("alertId")).append("|")
                  .append(res.getString("alertMessage")).append("|")
                  .append(res.getString("alertDate")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

    public void setTransactionCache(int userId)
    {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            System.out.println("Connection to server");
            ResultSet res = db.viewTransactions(userId);
            jedis.set("transactionList", makeTransactionString(res));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String makeTransactionString(ResultSet res)
    {
        StringBuilder sb = new StringBuilder();
        try {
            while (res.next()) {
                sb.append(res.getInt("transactionId")).append("|")
                  .append(res.getFloat("amount")).append("|")
                  .append(res.getString("reason")).append("|")
                  .append(res.getString("transactionDate")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

    public void setCartCache(int userId)
    {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            System.out.println("Connection to server");
            ResultSet res = db.viewCart(userId);
            jedis.set("cartList", makeCartString(res));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String makeCartString(ResultSet res)
    {
        StringBuilder sb = new StringBuilder();
        try {
            while (res.next()) {
                sb.append(res.getInt("cartId")).append("|")
                .append(res.getInt("productId")).append("|")
                .append(res.getString("productName")).append("|")
                .append(res.getString("productImage")).append("|")
                .append(res.getInt("productQuantity")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

    public String getData(String req)
    {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            return jedis.get(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
