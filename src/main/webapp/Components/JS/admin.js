document.addEventListener("DOMContentLoaded", () => {
    fetchUsers();

    var status;

    function fetchUsers() {
        fetch(`admin`, { 
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then((res) => res.json())
        .then((data) => {
            renderUserList(data.userList);
        })
        .catch((error) => {
            console.error('Fetch error:', error);
        });
    }

    function renderUserList(users) {
        const userListElement = document.getElementById("user-list");
        userListElement.innerHTML = ''; 

        if (users.length === 0) {
            userListElement.innerHTML = "<li>No users found.</li>";
            return;
        }

        users.forEach(user => {
            const userItem = document.createElement("li");
            userItem.classList.add("admin-details");

            userItem.innerHTML = `
                <div class="admin-id">${user.id}</div>
                <div class="admin-name">${user.name}</div>
                <div class="admin-role">${user.role}</div>
                <div class="admin-email">${user.email}</div>

                <div class="admin-add">
                    ${user.role === 'user' ?
                         `<button style="background-color: rgb(162, 225, 146);"
                           onclick="promoteToManager(${user.id})"><b>Make Manager</b></button>`
                          : 
                          '<button>x</button>'}
                </div>
                <div class="admin-delete">
                    ${user.role === 'manager' ? 
                        `<button style="background-color: rgb(232, 142, 142);"
                         onclick="demoteToUser(${user.id})"><b>Remove Manager</b></button>` 
                         : 
                         '<button>x</button>'}
                </div>
            `;

            if(user.role !== 'Superadmin')
                userListElement.appendChild(userItem);
        });
    }

    window.promoteToManager = function(userId) {
        fetch(`admin/${userId}`, { 
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body:  JSON.stringify({"type":"promote"})
        })
        .then((res) => res.json())
        .then((data) => {
            showNotification(data.message);
            fetchUsers();
        })
        .catch((error) => {
            console.error('Fetch error:', error);
        });
    };
    
    window.demoteToUser = function(userId) {
        fetch(`admin/${userId}`, { 
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body:  JSON.stringify({"type":"demote"})
        })
        .then((res) => res.json())
        .then((data) => {
            showNotification(data.message);
            fetchUsers();
        })
        .catch((error) => {
            console.error('Fetch error:', error);
        });
    };

});


function showNotification(message,status) {
    const notificationContainer = document.getElementById("notifications-container");
    const notification = document.createElement("div");
    notification.classList.add("notification");
    notification.textContent = message;

    notificationContainer.appendChild(notification);

    setTimeout(() => {
        notification.remove();
            
    }, 5000); 
}