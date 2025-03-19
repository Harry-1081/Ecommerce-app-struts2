function openPopup(role) {
    document.getElementById("popup").style.display = "flex";
    document.getElementById("otherRole").textContent = role;
    document.getElementById("r2").value = role;
}

function closePopup() {
    document.getElementById("popup").style.display = "none";
}

function setnewRole(role) {
    fetch(`login?currentRole=${role}`, { 
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then((res) =>  res.json())
    .then((data) => {
        renderAuditList(data.logList);
    })
    .catch((error) => {
        console.error('Fetch error:', error);
    });
}


document.getElementById("choose-role-form").addEventListener("submit", function(event) {
    event.preventDefault();
    var role = document.getElementById("status-menu").value;

    setnewRole(role);

    if(role == "admin")
        window.location.href = "/struts2/admin";
    else if(role == "manager")
        window.location.href = "/struts2/manager";
    else if(role == "Superadmin")
        window.location.href = "/struts2/Superadmin";
    else
        window.location.href = "/struts2/product";
});

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
        console.log(data.message)
        if(data.message === "user")
            window.location.href = "/struts2/product";
        else if((data.message).includes("Error"))
            showNotification(data.message);
        else
            openPopup(data.message);
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

