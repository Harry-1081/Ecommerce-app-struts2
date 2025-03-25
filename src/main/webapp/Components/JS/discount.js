document.getElementById("discount-form").addEventListener("submit", function(event) {
    event.preventDefault();
    fetch(`discount`, { 
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            "productId":document.getElementById(`discount-id`).value,
            "percentage":document.getElementById(`discount-percent`).value,
            "fromDate":document.getElementById(`discount-from`).value,
            "tillDate":document.getElementById(`discount-till`).value
        })
    })
    .then((res) => res.json())
    .then((data) => {
        showNotification(data.message);

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
    }, 3000); 
}
