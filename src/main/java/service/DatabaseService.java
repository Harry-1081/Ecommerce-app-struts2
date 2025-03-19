package service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import model.Account;
import model.Audit;
import model.Product;

public class DatabaseService {
    
    static Connection c;
    private KafkaProducerService kps = new KafkaProducerService();
    private KafkaConsumerService kcs = new KafkaConsumerService();
        
    public static Connection makeConnection() throws ClassNotFoundException, SQLException{
        String url = "jdbc:mysql://localhost:3306/ecom?allowPublicKeyRetrieval=true&useSSL=false";
        String username = "YOUR USERNAME";
        String password = "YOUR PASSWORD";
        Class.forName("com.mysql.cj.jdbc.Driver");
        c = DriverManager.getConnection(url, username, password);
        return c;
    }
    
    public ResultSet executeQuery(PreparedStatement query) throws SQLException {
        ResultSet result = null;
        try {
            if (query.toString().contains(": SELECT") || query.toString().contains(": WITH")) {
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
        String query = "SELECT userRole as role,a.id as id,password FROM ecom.account as a left join ecom.roles as r "+
        "on a.id = r.userId where email = ?";

        if(c==null)
            makeConnection();

        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, email);
        return executeQuery(st);   
    }

    public boolean createAccount(Account account) throws SQLException, ClassNotFoundException {
        String query = "INSERT INTO ecom.account (name, email, password) VALUES (?, ?, ?)";
        if(c==null)
            makeConnection();
        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, account.getName());   
        st.setString(2, account.getEmail()); 
        st.setString(3, account.getPassword()); 
        executeQuery(st);

        String kafkaMessage = String.format("{\"name\": \"%s\", \"email\": \"%s\", \"password\": \"%s\"}", 
            account.getName(), account.getEmail(), account.getPassword());
        kps.sendAccountCreationMessage(kafkaMessage);
        kcs.startAccountConsumer();

        return true;
    }
    
    public ResultSet viewAllProducts() throws SQLException, ClassNotFoundException {
        if(c==null)
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
                    makePurchase(userId,pid,quantityAvl,quantity,total,price,type);
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

    public void makePurchase(int userId, int pid, int quantityAvl, int quantity, float total, float price,String type) throws SQLException, ClassNotFoundException  {
        if(type.equals("single")){
            updateWalletBalance(userId, -total, "Bought product");

            String kafkaMessage = String.format("{\"productId\": \"%s\", \"quantity\": %d }", pid,quantity);
            kps.sendProductPurchaseMessage(kafkaMessage);
            kcs.startProductPurchaseConsumer(userId);
        }
        updateProduct(pid, quantityAvl-quantity, price,"-");
    }
    
    public void updateProduct(int pid, int quantity, float price,String info) throws SQLException {
        String query = "UPDATE ecom.inventory set quantity = ?, price = ? where productId = ?";
        PreparedStatement st = c.prepareStatement(query);

        st.setInt(1, quantity);
        st.setFloat(2, price);
        st.setInt(3, pid);
        
        String kafkaMessage = String.format("{\"productId\": \"%s\", \"quantity\": %d, \"price\": %.2f}",pid, quantity, price);
        kps.sendProductMessage(kafkaMessage);
        kcs.startProductConsumers(info);
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

        if(money>0){
            String kafkaMessage = String.format("{\"userId\": \"%s\", \"amount\": %.2f}",id, money);
            kps.sendWalletBalanceMessage(kafkaMessage);
            kcs.startWalletBalanceConsumers();
        }
        
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
        String query = "WITH cte as (SELECT COALESCE(userRole, 'user') as role, a.id, name, email FROM ecom.roles as r RIGHT JOIN ecom.account as a ON a.id = r.userId)"+
        " select * from cte where role != ?";
        if(c==null)
            makeConnection();
        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, "Superadmin");
        ResultSet res = executeQuery(st);
        return res;
    }
    
    public void removeAdmin(int id) throws SQLException,ClassNotFoundException {
        String query = "DELETE FROM ecom.roles where userId = ? and userRole = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setInt(1, id);
        st.setString(2, "admin");

        String kafkaMessage = String.format("{\"id\": \"%s\", \"role\": \"%s\"}",id,"Admin");
        kps.sendRoleMessage(kafkaMessage);
        kcs.startRoleConsumers("removed",0);

        executeQuery(st);
    }
    
    public boolean addAdmin(int id) throws SQLException,ClassNotFoundException {
        String query = "SELECT userRole from ecom.roles where userId = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setInt(1, id);
        ResultSet res = st.executeQuery();

        if(res.next()){ return false; }

        query = "INSERT INTO ecom.roles (userId, userRole) values (?, ?)";
        st = c.prepareStatement(query);
        st.setInt(1, id);
        st.setString(2, "admin");
        

        executeQuery(st);
        
        String kafkaMessage = String.format("{\"id\": \"%s\", \"role\": \"%s\"}",id,"Admin");
        kps.sendRoleMessage(kafkaMessage);
        kcs.startRoleConsumers("added",0);
        return true;
    }

    public void removeManager(int id,int userId) throws SQLException,ClassNotFoundException {
        String query = "DELETE FROM ecom.roles where userId = ? and userRole = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setInt(1, id);
        st.setString(2, "manager");

        String kafkaMessage = String.format("{\"id\": \"%s\", \"role\": \"%s\"}",id,"Manager");
        kps.sendRoleMessage(kafkaMessage);
        kcs.startRoleConsumers("removed",userId);

        executeQuery(st);
    }
    
    public boolean addManager(int id,int userId) throws SQLException,ClassNotFoundException {
        String query = "SELECT userRole from ecom.roles where userId = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setInt(1, id);
        ResultSet res = st.executeQuery();

        if(res.next()){ return false; }

        query = "INSERT INTO Ecom.roles (userId,userRole) values (?, ?)";
        st = c.prepareStatement(query);
        st.setInt(1, id);
        st.setString(2, "manager");

        executeQuery(st);

        String kafkaMessage = String.format("{\"id\": \"%s\", \"role\": \"%s\"}",id,"Manager");
        kps.sendRoleMessage(kafkaMessage);
        kcs.startRoleConsumers("added",userId);
        return true;
    }

    public void deleteProduct(int pid,int mid) throws SQLException, ClassNotFoundException {
        String query = "DELETE FROM ecom.inventory where productId = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setInt(1, pid);
        
        String kafkaMessage = String.format("{\"productId\": \"%s\"}",pid);
        kps.sendProductRemovalMessage(kafkaMessage);
        kcs.startProductRemovalConsumers(mid);
        
        executeQuery(st);   
    }
    
    public void createProduct(Product product,int mid) throws SQLException {
        String query = "INSERT INTO ecom.inventory (productName, quantity, price) VALUES (?, ?, ?)";
        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, product.getProductName());
        st.setInt(2, product.getQuantity());
        st.setFloat(3, product.getPrice());

        String kafkaMessage = String.format("{\"productName\": \"%s\", \"quantity\": %d, \"price\": %.2f}",product.getProductName(), 
            product.getQuantity(), product.getPrice());
        kps.sendNewProductMessage(kafkaMessage);
        kcs.startNewProductConsumers(mid);

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
            updateCart(rs.getInt("cartId"), rs.getInt("productQuantity")+quantity, userId);
            
        } else {
            String insertQuery = "INSERT INTO ecom.cart (userID, productId, productQuantity) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = c.prepareStatement(insertQuery);
            insertStmt.setInt(1, userId);
            insertStmt.setInt(2, pid);
            insertStmt.setInt(3, quantity);
            insertStmt.executeUpdate();

            String kafkaMessage = String.format("{\"userId\": \"%s\", \"productId\": %d, \"productQuantity\": %d}",userId,pid,quantity);
            kps.sendNewCartMessage(kafkaMessage);
            kcs.startNewCartConsumers();
        }
        
    }
    
    public void updateCart(int cartId, int quantity, int userId) throws SQLException, ClassNotFoundException{
        String updateQuery = "UPDATE ecom.cart SET productQuantity = ? WHERE cartId = ?";
        PreparedStatement updateStmt = c.prepareStatement(updateQuery);
        updateStmt.setInt(1, quantity);
        updateStmt.setInt(2, cartId);
        updateStmt.executeUpdate();

        String kafkaMessage = String.format("{\"userId\": \"%s\", \"cartId\": %d, \"productQuantity\": %d}",userId,cartId,quantity);
        kps.sendCartMessage(kafkaMessage);
        kcs.startCartConsumers();
    }

    public ResultSet viewCart(int userId) throws SQLException, ClassNotFoundException {
        String query = "SELECT i.productName,i.productId,c.productQuantity,i.productImage,c.cartId from ecom.cart as c inner join ecom.inventory as i on i.productId = c.productId and userID = ?";
        PreparedStatement st = c.prepareStatement(query);
        
        st.setInt(1, userId);
        
        ResultSet res = null;
        res = executeQuery(st);
        return res;
    }
    
    public void clearFromCart(int cartId,int userId) throws SQLException, ClassNotFoundException  {
        String query = "DELETE from ecom.cart where cartId = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setInt(1, cartId);
        executeQuery(st);

        String kafkaMessage = String.format("{\"userId\": \"%s\", \"cartId\": %d}",userId,cartId);
        kps.sendCartRemovalMessage(kafkaMessage);
        kcs.startCartRemovalConsumers();

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

        if(total>0){
            updateWalletBalance(userId, -total, "Purchased Cart");
            String kafkaMessage = String.format("{\"userId\": \"%s\", \"amount\": %.2f}",userId,total);
            kps.sendCartPurchaseMessage(kafkaMessage);
            kcs.startCartPurchaseConsumers();
        }
        if(total==0)
            return "You cannot purchase an empty cart";
        return "Cart successfully purchased";
    }

    public void clearCart(int userId) throws SQLException, ClassNotFoundException {
        String query = "DELETE from ecom.cart where userID = ?";
        PreparedStatement st = c.prepareStatement(query);
        st.setInt(1, userId);
        executeQuery(st);
    }
    
    public boolean isAccountExists(String email) throws SQLException, ClassNotFoundException {
        if(c==null)
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
        
    public ResultSet viewAllAudits(String role) throws SQLException, ClassNotFoundException { 
            String query = "SELECT * from ecom.log";
            if(role.equals("admin"))
                query+=" where doneBy != 'Superadmin'";
            query+=" order by auditId DESC";
            PreparedStatement st = c.prepareStatement(query);
            ResultSet res = executeQuery(st);
            return res;
        }
        
    public void addAudit(String action,String status,String doneBy,String parameter) throws SQLException {   
        String query = "INSERT INTO ecom.log (time, action, status, doneBy, parameter) values (?,?,?,?,?)";
        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        st.setString(2, action);
        st.setString(3, status);
        st.setString(4, doneBy);
        st.setString(5, parameter);
        executeQuery(st);
    }
    
    public ResultSet viewUserAudits(String userId, String startDate, String endDate, String action,String status,String role) throws SQLException {
        String query = "SELECT * FROM ecom.log WHERE doneBy LIKE ?";

        List<String> param = new ArrayList<>();

        if(":".equals(userId))
            param.add("%%");
        else
            param.add("%"+userId);
        
        if("admin".equals(role)){
            query+=" AND doneBy != 'Superadmin'";
        }

        if(!action.equals("")){
            query+="AND action like ?";
            param.add("%"+action+"%");
        }

        if(!startDate.equals("")){
            query+="AND STR_TO_DATE(time, '%Y-%m-%d %H:%i:%s') >= ?";
            param.add(startDate+" 00:00:00");
        }

        if(!endDate.equals("")){
            query+="AND STR_TO_DATE(time, '%Y-%m-%d %H:%i:%s') <= ?";
            param.add(endDate+" 23:59:59");
        }

        if(!status.equals("")){
            query+="AND status = ?";
            param.add(status);
        }

        query+="ORDER By auditId DESC";

        PreparedStatement st = c.prepareStatement(query);
        
        for(int i=0;i<param.size();i++){
            st.setString(i+1, param.get(i));
        }

        ResultSet res = executeQuery(st);
        return res;
    }


    public void generateReport() throws SQLException, ClassNotFoundException{
        
        List<Audit> logList = new ArrayList<>();

        if(c==null)
            makeConnection();
        
        String query = "SELECT * FROM ecom.log WHERE STR_TO_DATE(time, '%Y-%m-%d %H:%i:%s') >= ? AND STR_TO_DATE(time, '%Y-%m-%d %H:%i:%s') < ? AND (action = 'Purchased Product' OR action = 'Inventry change' OR action = 'Purchased cart');" ;
        PreparedStatement st = c.prepareStatement(query);
        st.setString(1, LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+" 00:00:00");
        st.setString(2, LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+" 23:59:59");
        ResultSet res = executeQuery(st);
        
        while(res.next()){
            
            int id = res.getInt("auditId");
            String time = res.getString("time");
            String action = res.getString("action");
            String status = res.getString("status");
            String doneBy = res.getString("doneBy");
            String parameter = res.getString("parameter");
            logList.add(new Audit(id, time, action, status, doneBy, parameter));
        }
        
        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 750); 
            
            contentStream.showText("Audit ID   Time               Action        Status   Done By  Parameter");
            contentStream.newLineAtOffset(0, -20); 
            
            for (Audit audit : logList) {
                String row = String.format("%-8d %-18s %-12s %-8s %-8s %s", 
                audit.getAuditId(), audit.getTime(), audit.getAction(), audit.getStatus(), audit.getDoneBy(), audit.getParameter());
                contentStream.showText(row);
                contentStream.newLineAtOffset(0, -20);
            }
            
            contentStream.endText();
            contentStream.close();
            document.save("src/main/webapp/static/DailyReport.pdf");
            document.close();
            System.out.println("PDF created successfully!");
        } catch (IOException e) {
            System.out.println("5");
            e.printStackTrace();
        }
    }
}