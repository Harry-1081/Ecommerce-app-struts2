package model;

public class Transaction 
{
    int transactionId;
    int userId;
    float amount;
    String reason;
    String transactionDate;

    Transaction(){}
    
    public Transaction(int transactionId, int userId, float amount, String reason, String transactionDate) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
        this.transactionDate = transactionDate;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    @Override
    public String toString() {
        return "," + userId + "," + amount + "," + reason + "," + transactionDate;
    }
}
