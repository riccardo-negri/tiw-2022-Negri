{ // avoid variables ending up in the global scope

    // page components
    const homeView = new HomeView(); // main controller
    const accountView = new AccountView();
    const transactionDetailsView = new TransactionDetailsView();

    window.addEventListener("load", () => {
        if (sessionStorage.getItem("username") == null) {
            window.location.href = "index.html";
        } else {
            homeView.show();
        }
    }, false);

    function HomeView() {
        this.show = function () {
            const self = this

            displayGenericModal("Titolo", "messaggio")

            makeCall("GET", "get-account-list", null, function (req) {
                if (req.readyState === 4) {
                    if (req.status === 200) {
                        const accounts = JSON.parse(req.responseText);
                        self.update(accounts)
                    } else if (req.status === 403) {
                        // TODO
                    } else {
                        // TODO
                    }
                }
            });
        }

        this.update = function (accounts) {
            const BASE = `<div id="home" class="row"><div class="col-12"><div class="card mb-4"><div class="card-header pb-0"><h6>Your accounts</h6></div><div class="card-body px-0 pt-0 pb-2"><div class="table-responsive p-0"><table class="table align-items-center mb-0"><thead><tr><th class="text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Account number</th><th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7 ps-2">Balance</th><th class="text-center text-uppercase text-secondary text-xxs font-weight-bolder opacity-7">Last activity</th></tr></thead><tbody id="account-list"></tbody></table></div></div></div></div></div>`
            document.getElementById("app-container").innerHTML = BASE

            accounts.forEach(account => {
                let markup = `<tr><td><div class="d-flex px-2 py-1"><div><img src="assets/img/logo_black.png" class="avatar avatar-sm me-3"alt="user1"></div><div class="d-flex flex-column justify-content-center"><h6 id="account-number" class="mb-0 text-sm">${accountCodeFormatter(account.code)}</h6></div></div></td><td class="align-middle text-center"><p class="text-sm font-weight-bold mb-0">${currencyFormatter.format(account.balance)}</p></td><td class="align-middle text-center"><span class="text-secondary text-xs font-weight-bold">${account.lastActivity}</span></td><td class="align-middle text-center"><a account="${account.id}" class="cursor-pointer text-primary font-weight-bold text-xs">VIEW</a></td></tr>`
                document.getElementById("account-list").innerHTML += markup;
            })
            let anchors = document.getElementById("account-list").getElementsByTagName("a")
            for (let anchor of anchors) {
                anchor.addEventListener("click", (e) => {
                    // TODO
                    console.log("HERE" + e.target.getAttribute("account"))
                    accountView.show(e.target.getAttribute("account"))
                }, false);
            }
        }
    }

    function AccountView() {
        this.show = function (accountID) {
            const self = this
            makeCall("GET", "get-account?id=" + accountID, null, function (req) {
                if (req.readyState === 4) {
                    if (req.status === 200) {
                        const response = JSON.parse(req.responseText);
                        const account = response[0];
                        const contacts = response[1];
                        const lastMonthTransactions = response[2];
                        const lastYearTransactions = response[3];
                        const previousTransactions = response[4];

                        self.update(account, contacts, lastMonthTransactions, lastYearTransactions, previousTransactions)
                    } else if (req.status === 403) {
                        // TODO
                    } else {
                        // TODO
                    }
                }
            });
        }

        this.update = function (account, contacts, lastMonthTransactions, lastYearTransactions, previousTransactions) {
            const time = "Updated at " + new Date().toLocaleTimeString("en-US")
            // TODO set user name and surname
            const BASE = `<div class="row mt-1"><div class="col-md-7"><!-- Info about account --><div class="row p-1"><div class="col-xl-8 mb-xl-0 mb-4"><div class="card bg-transparent shadow-xl"><div class="overflow-hidden position-relative border-radius-xl"style="background-image: url('assets/img/background_account.jpg');"><span class="mask bg-gradient-dark"></span><div class="card-body position-relative z-index-1 p-3"><p class="text-white text-sm opacity-8 mb-0">Bank Account Number</p><h5 id="account-number" class="text-white mt-1 mb-6 pb-4">${accountCodeFormatter(account.code)}</h5><div class="d-flex pt-1"><div class="d-flex"><div class="me-4"><p class="text-white text-sm opacity-8 mb-0">Account Owner</p><h6 id="account-holder" class="text-white mb-0">Mario Rossi</h6></div></div><div class="ms-auto w-20 d-flex align-items-end justify-content-end"><img class="w-40 mt-1" src="assets/img/logo_white.png" alt="logo"></div></div></div></div></div></div><div class="col-md-4"><div class="col"><div class="card"><div class="card-header mx-4 p-3 text-center"><div class="icon icon-shape icon-lg bg-gradient-primary shadow text-center border-radius-lg m-auto"><img class="w-65 mt-2 fas fa-landmark opacity-10" src="assets/img/balance.png"alt="logo"></div></div><div class="card-body pt-0 p-3 text-center"><h6 class="text-center mb-0">Balance</h6><span id="last-update-balance" class="text-xs">${time}</span><hr class="horizontal dark my-3"><h5 id="balance" class="mb-0">${currencyFormatter.format(account.balance)}</h5></div></div></div></div></div><!-- Transactions --><div class="row p-3"><div class="card h-100 mb-4"><div class="card-header pb-0 px-3"><div class="row"><div class="col-md-6"><h6 class="mb-0">Your Transactions</h6></div><div class="col-md-6 d-flex justify-content-end align-items-center"><i class="far fa-calendar-alt me-2"></i><small id="last-update-transactions">${time}</small></div></div></div><div id="transactions" class="card-body pt-4 p-3"></div></div></div></div><div class="col-md-5"><!-- Transaction form --><div class="row p-1"><div class=""><div class="card h-100"><div class="card-header pb-0 p-3"><div class="row"><div class="col-6 d-flex align-items-center"><h6 class="mb-0">Make a Transaction</h6></div></div></div><div class="card-body p-3 pb-0"><div class="card-body"><form id="form" role="form"><input type="hidden" name="origin-code" value=${account.code}><div class="row"><div class="col-md-6"><label>Beneficiary name</label><div class="mb-3"><input type="text" class="form-control" placeholder="Username"name="beneficiary-username" required></div></div><div class="col-md-6"><label>Beneficiary account</label><div class="mb-3"><input type="text" class="form-control"placeholder="1111 2222 3333" name="destination-code"required></div></div></div><label>Reason</label><div class="mb-3"><input type="text" class="form-control" placeholder="Reason" name="reason"required></div><div class="row"><div class="col-md-6"><label>Amount</label><div class="mb-3"><input type="number" class="form-control" placeholder="0" min="0"name="amount" required></div></div><div class="col-md-6"><div class="text-center pt-2"><button id="execute-button" class="btn bg-gradient-info w-100 mt-4 mb-0">Execute</button></div></div></div></form></div></div></div></div></div><!-- Contacts --><div class="row p-3"><div class="card"><div class="card-header pb-0 px-3"><h6 class="mb-0">Your Contacts</h6></div><div class="card-body pt-4 p-3"><ul id="contacts" class="list-group"></ul></div></div></div></div></div>`
            document.getElementById("app-container").innerHTML = BASE;

            // display the transactions
            const transactionsMainContainer = document.getElementById("transactions")
            if (lastMonthTransactions.length > 0) {
                let SECTION_TITLE = `<h6 class="text-uppercase text-body text-xs font-weight-bolder mb-3">Last month</h6>`
                transactionsMainContainer.innerHTML += SECTION_TITLE

                let TRANSACTION_CONTAINER = `<ul id="c" class="list-group">${markupFromTransactions(lastMonthTransactions)}</ul>`
                transactionsMainContainer.innerHTML += TRANSACTION_CONTAINER
            }
            if (lastYearTransactions.length > 0) {
                let SECTION_TITLE = `<h6 class="text-uppercase text-body text-xs font-weight-bolder mb-3">Last year</h6>`
                transactionsMainContainer.innerHTML += SECTION_TITLE

                let TRANSACTION_CONTAINER = `<ul id="c" class="list-group">${markupFromTransactions(lastYearTransactions)}</ul>`
                transactionsMainContainer.innerHTML += TRANSACTION_CONTAINER
            }
            if (previousTransactions.length > 0) {
                let SECTION_TITLE = `<h6 class="text-uppercase text-body text-xs font-weight-bolder mb-3">Previous</h6>`
                transactionsMainContainer.innerHTML += SECTION_TITLE

                let TRANSACTION_CONTAINER = `<ul id="c" class="list-group">${markupFromTransactions(previousTransactions)}</ul>`
                transactionsMainContainer.innerHTML += TRANSACTION_CONTAINER
            }

            // display the contacts
            const contactsMainContainer = document.getElementById("contacts")
            contactsMainContainer.innerHTML = markupFromContact(contacts)

            // add handler for the form
            document.getElementById("execute-button").addEventListener('click', (e) => {
                const form = e.target.closest("form");
                e.preventDefault();
                if (form.checkValidity()) {
                    if (validateAccountCode(form.elements["destination-code"].value) && validateAmount(form.elements["amount"].value, account.balance)) {
                        makeCall("POST", 'make-transaction', form, function (x) {
                            if (x.readyState === XMLHttpRequest.DONE) {
                                const message = x.responseText;
                                switch (x.status) {
                                    case 200:
                                        let transactionID = message;
                                        accountView.show(account.id)
                                        transactionDetailsView.show(transactionID)
                                        break;
                                    case 400: // bad request
                                        displayGenericModal("Bad request", message)
                                        break;
                                    case 500: // server error
                                        displayGenericModal("Server error", message)
                                        break;
                                    case 502:
                                        displayGenericModal("Bad gateway", message)
                                        break;

                                }
                            }
                        });
                    }
                } else {
                    form.reportValidity();
                }
            });

        }
    }

    function TransactionDetailsView() {
        this.show = function (transactionID) {
            const self = this
            makeCall("GET", "get-transaction-details?id=" + transactionID, null, function (req) {
                if (req.readyState === 4) {
                    if (req.status === 200) {
                        const response = JSON.parse(req.responseText);
                        const transaction = response[0];
                        const isAccountInContacts = response[1];
                        const accountOrigin = response[2];
                        const accountDestination = response[3];

                        self.update(transaction, isAccountInContacts, accountOrigin, accountDestination)
                    } else if (req.status === 403) {
                        // TODO
                    } else {
                        // TODO
                    }
                }
            });
        }

        this.update = function (transaction, isAccountInContacts, accountOrigin, accountDestination) {
            const MODAL = `<div id="modal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="modal-default" aria-hidden="true"><div class="modal-dialog modal- modal-dialog-centered modal-" role="document"><div class="modal-content"><div class="modal-header"><h6 class="modal-title" id="modal-title-default">The transaction was successfully executed</h6></div><div class="modal-body"><div class="pb-2"><div class="d-flex flex-column"><h6 class="mt-2 mb-2 text-sm">Transaction details:</h6><span class="mb-2 text-xs">Transaction ID: <span class="text-dark font-weight-bold ms-sm-2">${transaction.id}</span></span><span class="mb-2 text-xs">Transaction amount: <span class="text-dark ms-sm-2 font-weight-bold">${currencyFormatter.format(transaction.amount)}</span></span><span class="mb-2 text-xs">Transaction reason: <span class="text-dark ms-sm-2 font-weight-bold">${transaction.reason}</span></span><span class="mb-2 text-xs">Transaction timestamp: <span class="text-dark ms-sm-2 font-weight-bold">${transaction.timestamp}</span></span><h6 class="mt-2 mb-2 text-sm">Origin:</h6><span class="mb-2 text-xs">Account code: <span class="text-dark ms-sm-2 font-weight-bold">${accountCodeFormatter(transaction.origin)}</span></span><span class="mb-2 text-xs">Account owner: <span class="text-dark ms-sm-2 font-weight-bold">${transaction.sender.name + ' ' + transaction.sender.surname}</span></span><span class="mb-2 text-xs">Account balance before: <span class="text-dark ms-sm-2 font-weight-bold">${currencyFormatter.format(accountOrigin.balance + transaction.amount)}</span></span><span class="mb-2 text-xs">Account balance after: <span class="text-dark ms-sm-2 font-weight-bold">${currencyFormatter.format(accountOrigin.balance)}</span></span><h6 class="mt-2 mb-2 text-sm">Destination:</h6><span class="mb-2 text-xs">Account code: <span class="text-dark ms-sm-2 font-weight-bold">${accountCodeFormatter(transaction.destination)}</span></span><span class="mb-2 text-xs">Account owner: <span class="text-dark ms-sm-2 font-weight-bold">${transaction.receiver.name + ' ' + transaction.receiver.surname}</span></span><span class="mb-2 text-xs">Account balance before: <span class="text-dark ms-sm-2 font-weight-bold">${currencyFormatter.format(accountDestination.balance - transaction.amount)}</span></span><span class="mb-2 text-xs">Account balance after: <span class="text-dark ms-sm-2 font-weight-bold">${currencyFormatter.format(accountDestination.balance)}</span></span></div></div></div><div id="modal-footer" class="modal-footer"><button id="close-modal-button" type="button" class="btn mb-0 bg-gradient-primary">Close</button></div></div></div></div>`


            const container = document.getElementById("modal-container")
            container.innerHTML = MODAL

            // display the modal
            const modal = document.getElementById("modal");
            modal.style.display = "block"
            modal.style.opacity = "100"
            modal.style.background = "rgba(0,0,0,0.4)"
            modal.style.zIndex = "10000"



            // add to contacts
            if (!isAccountInContacts) {
                const ADD_CONTACT = `<button id="add-contact-button" type="button" class="btn mb-0 bg-gradient-primary">Add contact</button>`
                document.getElementById("modal-footer").innerHTML = ADD_CONTACT + document.getElementById("modal-footer").innerHTML
                document.getElementById("add-contact-button").addEventListener("click", () => {
                    makeCall("POST", 'add-contact?id=' + accountDestination.id, null, function (x) {
                        if (x.readyState === XMLHttpRequest.DONE) {
                            const message = x.responseText;
                            switch (x.status) {
                                case 200:
                                    accountView.show(accountOrigin.id)
                                    displayGenericModal("Action completed successfully", transaction.receiver.name + " " + transaction.receiver.surname + "'s account added successfully")
                                    break;
                                case 400: // bad request
                                    displayGenericModal("Bad request", message)
                                    break;
                                case 500: // server error
                                    displayGenericModal("Server error", message)
                                    break;
                                case 502:
                                    displayGenericModal("Bad gateway", message)
                                    break;

                            }
                        }
                    });
                    }
                )
            }

            // close button
            document.getElementById("close-modal-button").addEventListener("click", () => {
                    container.innerHTML = ""
                }
            )
        }
    }


}