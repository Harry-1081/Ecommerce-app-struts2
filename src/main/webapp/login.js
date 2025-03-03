function verifyLogin(user) {

    const options = {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(user),
    };

    fetch('login', options)
    .then((res) => res.json())
    .then((data) => {
        if(data.message === "Superadmin")
            window.location.href = "/basic-struts/Superadmin";
        else if (data.message === "admin")
            window.location.href = "/basic-struts/admin";
        else if(data.message === "manager")
            window.location.href = "/basic-struts/manager";
        else if(data.message === "user")
            window.location.href = "/basic-struts/product";
        else
        showNotification(data.message);
    })
    .catch((error) => {
        console.error('Fetch error:', error);
    });
}

document.getElementById("login-form").addEventListener("submit", function(event) {
    event.preventDefault();
    const user = {
        email: document.getElementById("login-email").value,
        password: document.getElementById("login-password").value,
    };
    verifyLogin(user);
});

function showNotification(message,status) {
    const notificationContainer = document.getElementById("notifications-container");
    const notification = document.createElement("div");
    notification.classList.add("notification");
    notification.textContent = message;

    notificationContainer.appendChild(notification);

    setTimeout(() => {
        notification.remove();
    }, 3000); 
}

