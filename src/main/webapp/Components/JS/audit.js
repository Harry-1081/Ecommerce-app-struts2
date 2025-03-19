
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
                document.getElementById('aheader-nav').style.display = 'block';
            } else{
                document.getElementById('saheader-nav').style.display = 'block';
            }
        });
}

function reRenderList(userId,startDate,endDate,action,status)
{
    fetch(`audit?id=${userId}&startDate=${startDate}&endDate=${endDate}&action=${action}&status=${status}`, { 
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
    const AuditList = document.getElementById("audit-list");
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
            auditLog.classList.add("audit-headers");
            auditLog.innerHTML =
                `<div id="audit-id">
                    <span>S.No</span>
                </div>

                <div id="audit-time">
                    <span>Time</span>
                </div>

                <div id="audit-action">
                    <span>Action</span>
                </div>

                <div id="audit-status">
                    <span>Status</span>
                </div>           

                <div id="audit-doneby">
                    <span>Performed By</span>
                </div>              

                <div id="audit-parameter">
                    <span>Parameters</span>
                </div>`
            AuditList.appendChild(auditLog);
        }
        const auditLog = document.createElement("li");
        auditLog.classList.add("audit-details");
        auditLog.innerHTML = ` 
                    <div id="audit-id">
                        <span>${i}</span>
                    </div>

                    <div id="audit-time">
                        <span>${audit.time}</span>
                    </div>

                    <div id="audit-action">
                        <span>${audit.action}</span>
                    </div>

                    <div id="audit-status">
                        <span>${audit.status}</span>
                    </div>           

                    <div id="audit-doneby">
                        <span>${audit.doneBy}</span>
                    </div>              

                    <div id="audit-parameter">
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
