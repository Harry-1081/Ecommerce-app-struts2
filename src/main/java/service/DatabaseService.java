package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import model.Account;
import model.Product;

public class DatabaseService {
    
    static Connection c;
    private KafkaProducerService kps = new KafkaProducerService();
        
    public static Connection makeConnection() throws ClassNotFoundException, SQLException{
        String url = "jdbc:mysql://localhost:3306/ecom?allowPublicKeyRetrieval=true&useSSL=false";
        String username = "YOUR SQL USERNAME";
        String password = "YOUR SQL PASSWORD";
        Class.forName("com.mysql.cj.jdbc.Driver");
        c = DriverManager.getConnection(url, username, password);
        return c;
    }
    
    public ResultSet executeQuery(PreparedStatement query) throws SQLException {
        ResultSet result = null;
        try {
            if (query.toString().contains(": SELECT")) {
                result = query.executeQuery();
                return result;
            } else {
                query.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
        return result;
    }

    public ResultSet verifyLogin(String email)  throws SQLException, ClassNotFoundException {
        String query = "SELECT role,id,password FROM ecom.account where email = ?";
        makeConnection();
        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, email);
        return executeQuery(st);
    }

    public boolean createAccount(Account account) throws SQLException, ClassNotFoundException {
        String query = "INSERT INTO ecom.account (name, email, password, role) VALUES (?, ?, ?, ?)";
        makeConnection();
        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, account.getName());   
        st.setString(2, account.getEmail()); 
        st.setString(3, account.getPassword()); 
        st.setString(4, "user");
        executeQuery(st);
        return true;
    }
    
    public ResultSet viewAllProducts() throws SQLException, ClassNotFoundException {
        makeConnection();
        String query = "SELECT * FROM inventory";
        PreparedStatement st = c.prepareStatement(query);
        ResultSet res = executeQuery(st);
        return res;
    }

    public String buyProduct(int pid,int quantity,int userId,String type) throws SQLException, ClassNotFoundException {
        String query = "SELECT quantity,price from ecom.inventory where productId = ?";
        PreparedStatement st = c.prepareStatement(query);
        
        st.setInt(1, pid);
        ResultSet res = executeQuery(st); 
        
        if(res.next()){

            int quantityAvl = res.getInt("quantity");
            float price = res.getFloat("price");

            if(quantityAvl>=quantity)
            {
                float total = quantity*price;
                if(checkWalletBalance(userId) >= total)
                {
                    makePurchase(userId,pid,quantityAvl-quantity,total,price,type);
                    return "Purchase successful";
                }
                else
                    return "Insufficient Wallet Balance";
            }
            else {
                return "Insufficient stock available";
            }

        }
        return "Error purchasing product";
    }

    public void makePurchase(int userId, int pid, int quantity, float total, float price,String type) throws SQLException, ClassNotFoundException  {
        if(type.equals("single"))
            updateWalletBalance(userId, -total, "Bought product");
        updateProduct(pid, quantity, price);
    }
    
    public void updateProduct(int pid, int quantity, float price) throws SQLException {
        String query = "UPDATE ecom.inventory set quantity = ?, price = ? where productId = ?";
        PreparedStatement st = c.prepareStatement(query);

        String kafkaMessage = String.format("{\"productId\": \"%s\", \"quantity\": %d, \"price\": %.2f}",pid, quantity, price);
        kps.sendMessage(kafkaMessage);
        
        st.setInt(1, quantity);
        st.setFloat(2, price);
        st.setInt(3, pid);
        
        executeQuery(st);
    }

    public float checkWalletBalance(int id) throws SQLException {
        String query = "SELECT wallet_balance from ecom.account where id = ?";
        PreparedStatement st = c.prepareStatement(query);
        
        st.setInt(1, id);
        
        ResultSet res = executeQuery(st);
        if(res.next()){
            return res.getFloat("wallet_balance");
        }
        return 0;
    }

    public void updateWalletBalance(int id, float money, String reason) throws SQLException, ClassNotFoundException  {
        String query = "UPDATE ecom.account set wallet_balance = (wallet_balance + ?) where id = ?";
        PreparedStatement st = c.prepareStatement(query);
        
        st.setFloat(1, money);
        st.setInt(2, id);
        
        executeQuery(st);
        
        addTransactions(id,money,reason,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    public void addTransactions(int userId,float money,String reason,String time) throws SQLException{

        String query = "INSERT INTO ecom.transactions (userId, amount, reason, transactionDate) VALUES (?, ?, ?, ?)";
        PreparedStatement st = c.prepareStatement(query);
        
        st.setInt(1, userId);
        st.setFloat(2, money);
        st.setString(3, reason);
        st.setString(4, time);
        
        executeQuery(st);

    }

    public ResultSet viewAllUsers() throws SQLException, ClassNotFoundException {
        String query = "SELECT * FROM ecom.account where role != ?";
        makeConnection();
        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, "Superadmin");
        ResultSet res = executeQuery(st);
        return res;
    }
    
    public void removeAdmin(int id) throws SQLException,ClassNotFoundException {
        String query = "UPDATE ecom.account set role = ? where id = ? and role = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, "user");
        st.setInt(2, id);
        st.setString(3, "admin");
        executeQuery(st);
    }
    
    public void addAdmin(int id) throws SQLException,ClassNotFoundException {
        String query = "UPDATE ecom.account set role = ? where id = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, "admin");
        st.setInt(2, id);
        executeQuery(st);
    }

    public void removeManager(int id) throws SQLException,ClassNotFoundException {
        String query = "UPDATE ecom.account set role = ? where id = ? and role = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, "user");
        st.setInt(2, id);
        st.setString(3, "manager");
        executeQuery(st);
    }
    
    public void addManager(int id) throws SQLException,ClassNotFoundException {
        String query = "UPDATE ecom.account set role = ? where id = ? and role = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, "manager");
        st.setInt(2, id);
        st.setString(3, "user");
        executeQuery(st);
    }

    public void deleteProduct(int pid) throws SQLException, ClassNotFoundException {
        String query = "DELETE FROM ecom.inventory where productId = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setInt(1, pid);
        executeQuery(st);   
    }

    public void createProduct(Product product) throws SQLException {
        String query = "INSERT INTO ecom.inventory (productName, quantity, price) VALUES (?, ?, ?)";
        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, product.getProductName());
        st.setInt(2, product.getQuantity());
        st.setFloat(3, product.getPrice());
        executeQuery(st);
    }

    public ResultSet viewTransactions(int userId) throws SQLException, ClassNotFoundException  {
        String query = "SELECT * FROM ecom.transactions where userId = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setInt(1, userId);
        ResultSet res = executeQuery(st);
        return res;
    }

    public void addToCart(int pid, int quantity, int userId) throws SQLException, ClassNotFoundException {
        String query = "SELECT productQuantity,cartId FROM ecom.cart WHERE userID = ? AND productId = ?";
        PreparedStatement stmt = c.prepareStatement(query);
        stmt.setInt(1, userId);
        stmt.setInt(2, pid);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            updateCart(rs.getInt("cartId"), rs.getInt("productQuantity")+quantity);
        } else {
            String insertQuery = "INSERT INTO ecom.cart (userID, productId, productQuantity) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = c.prepareStatement(insertQuery);
            insertStmt.setInt(1, userId);
            insertStmt.setInt(2, pid);
            insertStmt.setInt(3, quantity);
            insertStmt.executeUpdate();
        }
    }

    public void updateCart(int cartId, int quantity) throws SQLException, ClassNotFoundException{
        String updateQuery = "UPDATE ecom.cart SET productQuantity = ? WHERE cartId = ?";
        PreparedStatement updateStmt = c.prepareStatement(updateQuery);
        updateStmt.setInt(1, quantity);
        updateStmt.setInt(2, cartId);
        updateStmt.executeUpdate();
    }

    public ResultSet viewCart(int userId) throws SQLException, ClassNotFoundException {
        String query = "SELECT i.productName,i.productId,c.productQuantity,i.productImage,c.cartId from ecom.cart as c inner join ecom.inventory as i on i.productId = c.productId and userID = ?";
        PreparedStatement st = c.prepareStatement(query);
        
        st.setInt(1, userId);
        
        ResultSet res = null;
        res = executeQuery(st);
        return res;
    }
    
    public void clearFromCart(int cartId) throws SQLException, ClassNotFoundException  {
        String query = "DELETE from ecom.cart where cartId = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setInt(1, cartId);
        executeQuery(st);
    }
    
    public ResultSet checkAvailability(int pid) throws SQLException, ClassNotFoundException {
        String query = "SELECT * from ecom.inventory where productId = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setInt(1, pid);
        ResultSet res = null;
        res = executeQuery(st);
        return res;
    }
    
    public String purchaseCart(int userId) throws SQLException, ClassNotFoundException {
        ResultSet res = viewCart(userId);
        float total = 0;
        
        while(res.next()){
            int quantity = res.getInt("productQuantity");
            ResultSet res2 = checkAvailability(res.getInt("productId"));
            if(res2.next()){
                total += (res2.getInt("price") * quantity);
                if((res2.getInt("quantity") < quantity ))
                    return res.getString("productName") + " - Stock unavailable";
                else if (total>checkWalletBalance(userId))
                    return "Insufficient wallet balance";
            }
        }

        res = viewCart(userId);

        while(res.next()){
            buyProduct(res.getInt("productId"), userId, res.getInt("productQuantity"), "multiple");
        }

        clearCart(userId);
        if(total>0)
            updateWalletBalance(userId, -total, "Purchased Cart");
        return "Cart successfully purchased";
    }

    public void clearCart(int userId) throws SQLException, ClassNotFoundException {
        String query = "DELETE from ecom.cart where userID = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setInt(1, userId);
        executeQuery(st);
    }
    
    public boolean isAccountExists(String email) throws SQLException, ClassNotFoundException {
        makeConnection();
        String query = "SELECT COUNT(*) as count from ecom.account where email = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, email);
        ResultSet res = executeQuery(st);
        if(res.next()){
            if(res.getInt("count") >= 1)
                return true;
        }
        return false;
    }

    public ResultSet viewAllAlerts() throws SQLException {
        String query = "SELECT * from ecom.alerts order by alertId DESC";
        PreparedStatement st = c.prepareStatement(query);
        ResultSet res = executeQuery(st);
        return res;
    }
    
    public void addAlert(String message) throws SQLException, ClassNotFoundException {
        if(c==null)
            makeConnection();   
        String query = "INSERT INTO ecom.alerts (alertMessage, alertDate) values (?,?)";
        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, message);
        st.setString(2, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        executeQuery(st);
    }
}
