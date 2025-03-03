
document.addEventListener("DOMContentLoaded", () => { fetchtransactions();});

function fetchtransactions() {
    fetch(`wallet`, { 
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then((res) =>  res.json())
    .then((data) => {
        renderTransactionList(data.transactionList);
        document.getElementById('walletBalance').textContent = data.walletBalance;
    })
    .catch((error) => {
        console.error('Fetch error:', error);
    });
}

function renderTransactionList(transactions) {
    const TransactionList = document.getElementById("wallet-list");
    TransactionList.innerHTML = ''; 

    if (transactions.length === 0) {
        TransactionList.innerHTML = "<li>No Transactions found.</li>";
        return;
    }

    transactions.forEach(transaction => {
        const userTransaction = document.createElement("li");
        userTransaction.classList.add("wallet-details");

        userTransaction.innerHTML = ` 
                    <div id="wallet-id">
                        <span>${transaction.transactionId}</span>
                    </div>

                    ${transaction.amount > 0 ?
                        `<div id="wallet-amount" style="background-color: rgb(162, 225, 146);">
                                <span>${transaction.amount}</span>
                        </div>`
                    :
                        `<div id="wallet-amount" style="background-color: rgb(232, 142, 142);">
                                <span>${transaction.amount}</span>
                        </div>`
                    }

                    <div id="wallet-reason">
                        <span>${transaction.reason}</span>
                    </div>
                    <div id="wallet-date">
                        <span>${transaction.transactionDate}</span>
                    </div>                    
                `;

        if(userTransaction != null)
            TransactionList.appendChild(userTransaction);
    });
    
}

document.getElementById("addMoney-form").addEventListener("submit", function(event) {
    event.preventDefault();
    fetch(`wallet`, { 
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({money:`${document.getElementById(`moneyadd-input`).value}`})
    })  
    .then((res) => res.json())
    .then((data) => {
        showNotification(data.message);
        fetchtransactions();
    })
    .catch((error) => {
        console.log('Fetch error:', error);
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
