package model;

public class Account 
{

    int id;
    String name;
    String email;
    String password;
    float wallet_balance;
    String role;

    public Account(){}
    
    public Account(int id, String name, String email, String password, float wallet_balance,String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.wallet_balance = wallet_balance;
        this.role = role;
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public float getWallet_balance() {
        return wallet_balance;
    }

    public void setWallet_balance(float wallet_balance) {
        this.wallet_balance = wallet_balance;
    }

    @Override
    public String toString() {
        return "," + name +","+ password +","+ (role==null?"user":role)+","+ email +"," + wallet_balance;
    }
    
}
