function openPopup(cartId, currentQuantity) {
    document.getElementById("popup").style.display = "flex";
    document.getElementById("cartIdInput").value = cartId;
    document.getElementById("quantityInput").value = currentQuantity;
}

function closePopup() {
    document.getElementById("popup").style.display = "none";
}

document.addEventListener("DOMContentLoaded", () => { fetchCart();});

function fetchCart() {
    fetch(`cart`, { 
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then((res) => res.json())
    .then((data) => {
        renderCartList(data.cartList);
    })
    .catch((error) => {
        console.error('Fetch error:', error);
    });
}
    

function renderCartList(carts) {
    const CartList = document.getElementById("cart-list");
    CartList.innerHTML = ''; 
    
    if (carts.length === 0) {
        CartList.innerHTML = "<li>No Products found in Cart.</li>";
        return;
    }
    
    carts.forEach(cart => {
        const userCart = document.createElement("li");
    
        userCart.innerHTML = `
            <div id="cart-array">
                <div id="cart-array1">
                    <img id="cart-image" src="${cart.productImage}"><br>
                </div>
                <div id="cart-array2">
                    <h3><b>${cart.productName} x ${cart.productQuantity} </b></h3>
                </div>
                <div id="cart-array3">
                    <button style="background-color: rgb(162, 225, 146); height: 30px; padding: 5px;" 
                    onclick="openPopup(${cart.cartId}, ${cart.productQuantity})"><b>Edit Cart</b></button>
                </div>
                <div id="cart-array4">
                    <button onclick="removeFromCart(${cart.cartId})"
                     style="background-color: rgb(232, 142, 142); height: 30px; padding: 5px;"><b>Remove</b></button>
                </div>
            </div> 
            `;

        if(userCart != null)
            CartList.appendChild(userCart);
    });

    window.removeFromCart = function(cartId) {
        fetch(`cart/${cartId}`, { 
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then((res) => res.json())
        .then((data) => {
            showNotification(data.message);
            fetchCart();
        })
        .catch((error) => {
            console.error('Fetch error:', error);
        });
    };
}

document.getElementById("buyCart-form").addEventListener("submit", function(event) {
    event.preventDefault();
    fetch('purchase', { 
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        },
    })
    .then((res) => res.json())
    .then((data) => {
        showNotification(data.message);
        fetchCart();
    })
    .catch((error) => {
        console.error('Fetch error:', error);
    });
});

document.getElementById("editCart-form").addEventListener("submit", function(event) {
    event.preventDefault();
    fetch(`cart/${document.getElementById(`cartIdInput`).value}`, { 
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            "quantity":document.getElementById(`quantityInput`).value
        })
    })
    .then((res) => res.json())
    .then((data) => {
        showNotification(data.message);
        closePopup();
        fetchCart();
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