
document.addEventListener("DOMContentLoaded", () => { fetchAudits();});

function fetchAudits() {
    fetch(`audit`, { 
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then((res) =>  res.json())
    .then((data) => {
        renderAuditList(data.logList);
        fetchHeader(data.role);
    })
    .catch((error) => {
        console.error('Fetch error:', error);
    });
}

function fetchHeader(role)
{
    fetch('Components/HTML/header.html')
        .then(response => response.text())
        .then(data => {
            document.getElementById('header-container').innerHTML = data;
            if("admin"== role){
                document.getElementById('v2').disabled = true;
                document.getElementById('v3').disabled = true;
                document.getElementById('v4').disabled = true;
                document.getElementById('v5').disabled = true;
                document.getElementById('v7').disabled = true;
                document.getElementById('v8').disabled = true;
                document.getElementById('v9').disabled = true;
                document.getElementById('aheader-nav').style.display = 'block';
            }
            else if("manager"== role) {
                document.getElementById('v2').disabled = true;
                document.getElementById('v4').disabled = true;
                document.getElementById('v5').disabled = true;
                document.getElementById('v6').disabled = true;
                document.getElementById('v9').disabled = true;
                document.getElementById('mheader-nav').style.display = 'block';
            }
            else{
                document.getElementById('v2').disabled = true;
                document.getElementById('v3').disabled = true;
                document.getElementById('v6').disabled = true;
                document.getElementById('v7').disabled = true;
                document.getElementById('v8').disabled = true;
                document.getElementById('header-nav').style.display = 'block';
            }
        });
}

function reRenderList(startDate, endDate,action,status)
{
    fetch(`audit?startDate=${startDate}&endDate=${endDate}&action=${action}&status=${status}`, { 
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

function renderAuditList(audits) {
    const AuditList = document.getElementById("userlog-list");
    AuditList.innerHTML = ''; 

    if (audits.length === 0) {
        AuditList.innerHTML = `<li style="height:30px;">No Logs found.</li>`;
        return;
    }

    var i=0; 
    audits.forEach(audit => {
        i++;
        if(i==1){
            const auditLog = document.createElement("li");
            auditLog.classList.add("userlog-headers");
            auditLog.innerHTML =
                `<div id="userlog-id">
                    <span>S.No</span>
                </div>

                <div id="userlog-time">
                    <span>Time</span>
                </div>

                <div id="userlog-action">
                    <span>Action</span>
                </div>

                <div id="userlog-status">
                    <span>Status</span>
                </div>           

                <div id="userlog-parameter">
                    <span>Parameters</span>
                </div>`
            AuditList.appendChild(auditLog);
        }
        const auditLog = document.createElement("li");
        auditLog.classList.add("userlog-details");
        auditLog.innerHTML = ` 
                    <div id="userlog-id">
                        <span>${i}</span>
                    </div>

                    <div id="userlog-time">
                        <span>${audit.time}</span>
                    </div>

                    <div id="userlog-action">
                        <span>${audit.action}</span>
                    </div>

                    <div id="userlog-status">
                        <span>${audit.status}</span>
                    </div>           

                    <div id="userlog-parameter">
                        <span>${audit.parameter}</span>
                    </div>                    
                `;
        if(auditLog != null)
            AuditList.appendChild(auditLog);
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
