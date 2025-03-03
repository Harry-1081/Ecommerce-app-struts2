
document.addEventListener("DOMContentLoaded", () => { fetchAlerts();});

function fetchAlerts() {
    fetch(`alert`, { 
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then((res) =>  res.json())
    .then((data) => {
        renderAlertList(data.alertList);
    })
    .catch((error) => {
        console.error('Fetch error:', error);
    });
}

function renderAlertList(alerts) {
    const AlertList = document.getElementById("alert-list");
    AlertList.innerHTML = ''; 

    if (alerts.length === 0) {
        AlertList.innerHTML = "<li>No Alerts found.</li>";
        return;
    }

    alerts.forEach(alert => {
        const managerAlert = document.createElement("li");
        managerAlert.classList.add("alert-details");

        managerAlert.innerHTML = ` 
                    <div id="alert-id">
                        <span>${alert.alertId}</span>
                    </div>

                    <div id="alert-reason">
                        <span>${alert.alertMessage}</span>
                    </div>
                    <div id="alert-date">
                        <span>${alert.alertDate}</span>
                    </div>                    
                `;

        if(managerAlert != null)
            AlertList.appendChild(managerAlert);
    });
    
}


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
