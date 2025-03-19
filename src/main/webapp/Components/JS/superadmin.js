document.addEventListener("DOMContentLoaded", () => {
    fetchUsers();

    function fetchUsers() {
        fetch(`Superadmin`, { 
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
            userItem.classList.add("sadmin-details");

            userItem.innerHTML = `
                <div class="sadmin-id">${user.id}</div>
                <div class="sadmin-name">${user.name}</div>
                <div class="sadmin-role">${user.role}</div>
                <div class="sadmin-email">${user.email}</div>
                <div class="sadmin-add">
                    ${user.role !== 'admin' 
                        ? `<button style="background-color: rgb(162, 225, 146);"
                        onclick="promoteToAdmin(${user.id})"><b>Make Admin</b></button>`
                        : 
                        '<button>x</button>'}
                </div>
                <div class="sadmin-delete">
                    ${user.role === 'admin' 
                        ? `<button style="background-color: rgb(232, 142, 142);"
                        onclick="demoteToUser(${user.id})"><b>Remove Admin</b></button>`
                        : 
                        '<button>x</button>'}
                </div>
            `;

            if(user.role !== 'Superadmin')
                userListElement.appendChild(userItem);
        });
    }

    window.promoteToAdmin = function(userId) {
        fetch(`Superadmin/${userId}`, { 
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
        fetch(`Superadmin/${userId}`, { 
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