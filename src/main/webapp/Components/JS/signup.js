function createAccount(account) {

    console.log("Sending data:", JSON.stringify(account));

    const options = {
        method: 'POST',
        headers: {
            'Content-Type': 'application/JSON',
        },
        body:  JSON.stringify(account),
    };

    fetch('signup', options)
    .then((res) => res.json())
    .then((data) => {
        console.log(data.message);
        showNotification(data.message);
    })
    .catch((error) => {
        console.error('Fetch error:', error);
    });

}

document.getElementById("signup-form").addEventListener("submit", function(event) {
    event.preventDefault();
    const account = {
        name: document.getElementById("signup-username").value,
        email: document.getElementById("signup-email").value,
        password: document.getElementById("signup-password").value,
        wallet_balance: 0.0,
        role: "user"
    };
    createAccount(account);
});

function showNotification(message) {
    const notificationContainer = document.getElementById("notifications-container");

    const notification = document.createElement("div");
    notification.classList.add("notification");
    notification.textContent = message;

    notificationContainer.appendChild(notification);

    setTimeout(() => {
        notification.remove();
        if(message ==="Account Created")
            window.location.href = "/struts2/login";
    }, 3000); 
}
