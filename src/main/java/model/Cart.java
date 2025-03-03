package model;

public class Cart {
    int cartId;
    int userId;
    int productId;
    String productName;
    String productImage;
    int productQuantity;

    Cart(){}

    public Cart(int cartId, int userId, int productId, int productQuantity, String productName, String productImage) {
        this.cartId = cartId;
        this.userId = userId;
        this.productId = productId;
        this.productQuantity = productQuantity;
        this.productName = productName;
        this.productImage = productImage;
    }

    public int getCartId() {
        return cartId;
    }
    public void setCartId(int cartId) {
        this.cartId = cartId;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public int getProductId() {
        return productId;
    }
    public void setProductId(int productId) {
        this.productId = productId;
    }
    public int getProductQuantity() {
        return productQuantity;
    }
    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }
    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public String getProductImage() {
        return productImage;
    }
    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }
    
    @Override
    public String toString() {
        return "," + userId + "," + productId + "," + productQuantity  + "," + productName + "," + productImage;
    }
}
