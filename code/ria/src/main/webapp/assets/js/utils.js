function makeCall(method, url, formElement, callBack, reset = true) {
    const req = new XMLHttpRequest(); // visible by closure
    req.onreadystatechange = function () {
        callBack(req)
    }; // closure
    req.open(method, url);
    if (formElement == null) {
        req.send();
    } else {
        req.send(new FormData(formElement));
    }
    if (formElement !== null && reset === true) {
        formElement.reset();
    }
}

const currencyFormatter = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2
})

const accountCodeFormatter = (code) => {
    return code.slice(0, 4) + " " + code.slice(4, 8) + " " + code.slice(8, 12)
}

const markupFromTransactions = (transactions) => {
    let markup = ""
    transactions.forEach(transaction => {
        if (transaction.receiver.username.normalize().trim() === sessionStorage.getItem("username").normalize().trim()) {
            let POSITIVE_TRANSACTION = `<li class="list-group-item border-0 d-flex justify-content-between ps-0 mb-2 border-radius-lg"><div class="d-flex align-items-center"><button class="btn disabled btn-icon-only btn-rounded btn-outline-success mb-0 me-3 btn-sm d-flex align-items-center justify-content-center"><svg xmlns="http://www.w3.org/2000/svg" class="" fill="none"viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round"d="M7 11l5-5m0 0l5 5m-5-5v12"/></svg></button><div class="d-flex flex-column"><h6 class="mb-1 text-dark text-sm">${transaction.sender.name + " " + transaction.sender.surname}</h6><span class="text-xs">Sent from ${accountCodeFormatter(transaction.origin)} on ${transaction.timestamp}</span><span class="text-xs">Reason: ${transaction.reason}</span></div></div><div class="d-flex align-items-center text-success text-gradient text-sm font-weight-bold">+ ${currencyFormatter.format(transaction.amount)}</div></li>`
            markup += POSITIVE_TRANSACTION
        } else {
            let NEGATIVE_TRANSACTION = `<li class="list-group-item border-0 d-flex justify-content-between ps-0 mb-2 border-radius-lg"><div class="d-flex align-items-center"><button class="btn disabled btn-icon-only btn-rounded btn-outline-danger mb-0 me-3 btn-sm d-flex align-items-center justify-content-center"><svg xmlns="http://www.w3.org/2000/svg" class="" fill="none"viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round"d="M17 13l-5 5m0 0l-5-5m5 5V6"/></svg></button><div class="d-flex flex-column"><h6 class="mb-1 text-dark text-sm">${transaction.receiver.name + " " + transaction.receiver.surname}</h6><span class="text-xs">Sent to ${accountCodeFormatter(transaction.destination)} on ${transaction.timestamp}</span><span class="text-xs">Reason: ${transaction.reason}</span></div></div><div class="d-flex align-items-center text-danger text-gradient text-sm font-weight-bold">- ${currencyFormatter.format(transaction.amount)}</div></li>`
            markup += NEGATIVE_TRANSACTION
        }
    })
    return markup
}

const markupFromContact = (contacts) => {
    let markup = ""
    contacts.forEach(contact => {
        let accountsMarkup = ""
        contact.accountList.forEach(code => {
            let ACCOUNT_LINE = `<span class="mb-2 text-xs">Account number: <span class="text-dark ms-sm-2 font-weight-bold">${accountCodeFormatter(code)}</span></span>`
            accountsMarkup += ACCOUNT_LINE
        })

        let CONTACT_LINE = `<li class="list-group-item border-0 d-flex p-4 mb-2 bg-gray-100 border-radius-lg"><div class="d-flex flex-column"><h6 class="mb-3 text-sm">${contact.user.name + ' ' + contact.user.surname}</h6><span class="mb-2 text-xs">Username: <span class="text-dark font-weight-bold ms-sm-2">${contact.user.username}</span></span>${accountsMarkup}</div></li>`
        markup += CONTACT_LINE
    })

    return markup
}

const displayGenericModal = (title, message) => {
    const container = document.getElementById("modal-container")
    const MODAL = `<div id="modal"  class="modal fade" tabindex="-1" role="dialog" aria-labelledby="modal-default" aria-hidden="true"><div class="modal-dialog modal- modal-dialog-centered modal-" role="document"><div class="modal-content"><div class="modal-header"><h6 class="modal-title" id="modal-title-default">${title}</h6></div><div class="modal-body">${message}</div><div class="modal-footer"><button id="close-modal-button" type="button" class="btn mb-0 bg-gradient-primary">Close</button></div></div></div></div>`
    container.innerHTML = MODAL

    // display the modal
    const modal = document.getElementById("modal");
    modal.style.display = "block"
    modal.style.opacity = "100"
    modal.style.background = "rgba(0,0,0,0.4)"
    modal.style.zIndex = "10000"

    // close button
    modal.getElementsByTagName("button")[0].addEventListener("click", () => {
        container.innerHTML = ""
    })

}

const validateAccountCode = (code) => {
    let regex = /^\d+$/;
    code = code.trim().replaceAll(" ", "")
    if (regex.test(code) && code.length === 12) return true
    displayGenericModal("Invalid account number", "Please insert a valid beneficiary account number")
    return false
}

const validateAmount = (amount, balance) => {
    amount = parseInt(amount)
    balance = parseInt(balance)
    if(amount > 0 && amount <= balance) return true
    displayGenericModal("Invalid amount", "Please insert a valid amount that is more than zero and within your balance")
    return false
}