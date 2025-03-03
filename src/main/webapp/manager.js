function openEditPopup(productId, productName, price, quantity) {
    document.getElementById("editPopup").style.display = "block";
    document.getElementById("EproductId").value = productId;
    document.getElementById("EproductName").value = productName;
    document.getElementById("EproductPrice").value = price;
    document.getElementById("EproductQuantity").value = quantity;
}

function closeEditPopup() {
    document.getElementById("editPopup").style.display = "none";
}
function openAddPopup(productName, price, quantity) {
    document.getElementById("AddPopup").style.display = "block";
    document.getElementById("AproductName").value = productName;
    document.getElementById("AproductPrice").value = price;
    document.getElementById("AproductQuantity").value = quantity;
}

function closeAddPopup() {
    document.getElementById("AddPopup").style.display = "none";
}

// START

document.addEventListener("DOMContentLoaded", () => { fetchProducts(); });

function fetchProducts() {
    fetch(`manager`, { 
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
    const productList = document.getElementById("product-list");
    productList.innerHTML = ''; 
    
    if (products.length === 0) {
        productList.innerHTML = "<li>No Products found.</li>";
        return;
    }

    products.forEach(product => {
        const prodItem = document.createElement("li");
        prodItem.classList.add("manager-details");

        prodItem.innerHTML = `
            <div id="manager-pid">${product.productId}</div>
            <div id="manager-pname">${product.productName}</div>
            <div id="manager-price">${product.price}</div>
            <div id="manager-quantity">${product.quantity}</div>

            <div id="manager-edit">
                <button type="button" style="background-color: rgb(162, 225, 146);" 
                    onclick="openEditPopup(${product.productId},'${product.productName}',${product.price},${product.quantity})">
                    <b>Edit Product</b>
                </button>
            </div>

            <div id="manager-delete">
                <button onclick="deleteProduct(${product.productId})"
                    style="background-color: rgb(232, 142, 142);"><b>Delete Product</b></button>
            </div>
                `;

        if(prodItem != null)
            productList.appendChild(prodItem);
        });
    }
    
    window.deleteProduct = function(productId) {
        fetch(`manager/${productId}`, { 
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
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


document.getElementById("add-form").addEventListener("submit", function(event) {
    event.preventDefault();
    const product = {
        productName : document.getElementById("AproductName").value,
        price : document.getElementById("AproductPrice").value,
        quantity : document.getElementById("AproductQuantity").value,
    };
    const options = {
        method: 'POST',
        headers: {
            'Content-Type': 'application/JSON',
        },
        body:  JSON.stringify(product),
    };

    fetch('manager', options)
    .then((res) => res.json())
    .then((data) => {
        showNotification(data.message);
        closeAddPopup();
        fetchProducts();
    })
    .catch((error) => {
        console.error('Fetch error:', error);
    });
});

document.getElementById("edit-form").addEventListener("submit", function(event) {
    event.preventDefault();
    const product = {
        productName : document.getElementById("EproductName").value,
        price : document.getElementById("EproductPrice").value,
        quantity : document.getElementById("EproductQuantity").value
    };
    const options = {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/JSON',
        },
        body:  JSON.stringify(product),
    };

    fetch(`manager/${document.getElementById("EproductId").value}`, options)
    .then((res) => res.json())
    .then((data) => {
        showNotification(data.message);
        closeEditPopup();
        fetchProducts();
    })
    .catch((error) => {
        console.error('Fetch error:', error);
    });
});

function showNotification(message) {
    const notificationContainer = document.getElementById("notifications-container");
    const notification = document.createElement("div");
    notification.classList.add("notification");
    notification.textContent = message;

    notificationContainer.appendChild(notification);

    setTimeout(() => {
        notification.remove();
    }, 5000); 
}
