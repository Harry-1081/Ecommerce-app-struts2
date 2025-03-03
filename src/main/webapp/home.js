document.addEventListener("DOMContentLoaded", () => { fetchProducts(); });

function fetchProducts() {
    fetch(`product`, { 
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then((res) => res.json())
    .then((data) => {
        renderProductList(data.productList);
    })
    .catch((error) => {
        console.error('Fetch error:', error);
    });
}

function renderProductList(products) {

    const productList = document.getElementById("home-page");
    productList.innerHTML = ''; 

    if (products.length === 0) {
        productList.innerHTML = "<li>No Products found.</li>";
        return;
    }

    products.forEach(product => {
        const prodItem = document.createElement("li");

        prodItem.innerHTML = `
                <div id="product-array">
                    <div id="product-array1" onclick="showProdData('${product.productId}')">
                        <img id="product-image" src="${product.productImage}"><br>
                    </div>
                    <div id="product-array2" onclick="showProdData('${product.productId}')">
                        <span><b>Product Name : ${product.productName}</b></span><br>
                        <span><b>Product Price : ${product.price} Rs</b></span><br>
                    </div>

                    <div>

                    ${product.quantity > 0 ?
                        `<div id="product-input">
                            <input type="number" name="quantity" id="quantity-buy${product.productId}" min="1" max="${product.quantity}" value="1">
                            <button onclick="buyProduct(${product.productId})" style="margin-left: 10px; width: 90px;" id="buy-button">Buy Product</button>
                        </div>
                        <div id="product-input">
                            <input type="number" name="quantity" id="quantity-cart${product.productId}" min="1" max="${product.quantity}" value="1">
                            <button onclick="addToCart(${product.productId})" id="addtocart-button" style="margin-left: 10px;">Add To Cart</button>
                        </div>`
                        :
                        `<div id="product-input">
                            <input type="number" name="quantity" id="quantity-buy${product.productId}" min="1" max="${product.quantity}" value="0" disabled>
                            <button onclick="showNotification(${product.productName}+"is out of stock")" style="margin-left: 10px; width: 90px;" id="buy-button" disabled>Buy Product</button>
                        </div>
                        <div id="product-input">
                            <input type="number" name="quantity" id="quantity-cart${product.productId}" min="1" max="${product.quantity}" value="0" disabled>
                            <button onclick="showNotification(${product.productName}+"is out of stock")" id="buy-button" style="margin-left: 10px; width: 90px;" disabled>Add To Cart</button>
                        </div>`
                    }
                    </div>
                </div>
                `;

        if(prodItem != null)
            productList.appendChild(prodItem);

    });

    window.addToCart = function(productId) {
        fetch('cart', { 
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body:  JSON.stringify({
                "productId":productId,
                "quantity":document.getElementById(`quantity-cart${productId}`).value
            })
        })
        .then((res) => res.json())
        .then((data) => {
            showNotification(data.message);
            fetchProducts();
        })
        .catch((error) => {
            console.error('Fetch error:', error);
        });
    };

    window.showProdData = function(productId) {
        fetch(`product/${productId}`, { 
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then((res) => res.json())
        .then((data) => {
            renderProductDetail(data.productList);
        })
        .catch((error) => {
            console.error('Fetch error:', error);
        });
    };

    window.buyProduct = function(productId) {
        fetch(`purchase/${productId}`, { 
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body:  JSON.stringify({
                "quantity":document.getElementById(`quantity-buy${productId}`).value
            })
        })
        .then((res) => res.json())
        .then((data) => {
            showNotification(data.message);
            fetchProducts();
        })
        .catch((error) => {
            console.error('Fetch error:', error);
        });
    };

}

function renderProductDetail(products) {

    const productList = document.getElementById("home-page");
    productList.innerHTML = ''; 

    if (products.length === 0) {
        productList.innerHTML = "<li>No Products found.</li>";
        return;
    }

    products.forEach(product => {
        const prodItem = document.createElement("li");

        prodItem.innerHTML = `
                <div id="detail" style="margin:50px; display:flex; flex-direction:column;">
                <button style="margin-bottom:30px; width:150px;" onclick="fetchProducts()">Back</button>
                    <div>
                        <img style="margin-bottom:30px; height:300px; width:300px;"
                         src="${product.productImage}"><br>
                    </div>
                    <div>
                        <span><b>Product Name : ${product.productName}</b></span><br>
                        <span><b>Product Price : ${product.price} Rs</b></span><br>
                        <span><b>Quantity Available : ${product.quantity} Nos</b></span><br>
                    </div>
                </div>
                `;

        if(prodItem != null)
            productList.appendChild(prodItem);
    });

}

function showNotification(message) {
    if(message==="") {
        message ="You have to login to access this feature";
        status = 200;
    }
    const notificationContainer = document.getElementById("notifications-container");
    const notification = document.createElement("div");
    notification.classList.add("notification");
    notification.textContent = message;

    notificationContainer.appendChild(notification);

    setTimeout(() => {
        notification.remove();
    }, 5000); 
}