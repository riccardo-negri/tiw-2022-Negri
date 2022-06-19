const currencyFormatter = new Intl.NumberFormat('en-US', {
    style: 'currency', currency: 'USD', minimumFractionDigits: 2
})

const accountCodeFormatter = (code) => {
    return code.slice(0, 4) + " " + code.slice(4, 8) + " " + code.slice(8, 12)
}

const markupFromTransactions = (transactions) => {
    let markup = ""
    let currAccount = document.getElementById("account-number").innerText.trim().replaceAll(" ", "")
    transactions.forEach(transaction => {
        if (transaction.destination.normalize().trim() === currAccount) {
            let POSITIVE_TRANSACTION = `<li class="list-group-item border-0 d-flex justify-content-between ps-0 mb-2 border-radius-lg"><div class="d-flex align-items-center"><button class="btn disabled btn-icon-only btn-rounded btn-outline-success mb-0 me-3 btn-sm d-flex align-items-center justify-content-center"><svg xmlns="http://www.w3.org/2000/svg" class="" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M7 11l5-5m0 0l5 5m-5-5v12"/></svg></button><div class="d-flex flex-column"><h6 class="mb-1 text-dark text-sm">${transaction.sender.name + " " + transaction.sender.surname}</h6><span class="text-xs">Sent from ${accountCodeFormatter(transaction.origin)} on ${transaction.timestamp}</span><span class="text-xs">Reason: ${transaction.reason}</span></div></div><div class="d-flex align-items-center text-success text-gradient text-sm font-weight-bold">+ ${currencyFormatter.format(transaction.amount)}</div></li>`
            markup += POSITIVE_TRANSACTION
        } else {
            let NEGATIVE_TRANSACTION = `<li class="list-group-item border-0 d-flex justify-content-between ps-0 mb-2 border-radius-lg"><div class="d-flex align-items-center"><button class="btn disabled btn-icon-only btn-rounded btn-outline-danger mb-0 me-3 btn-sm d-flex align-items-center justify-content-center"><svg xmlns="http://www.w3.org/2000/svg" class="" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M17 13l-5 5m0 0l-5-5m5 5V6"/></svg></button><div class="d-flex flex-column"><h6 class="mb-1 text-dark text-sm">${transaction.receiver.name + " " + transaction.receiver.surname}</h6><span class="text-xs">Sent to ${accountCodeFormatter(transaction.destination)} on ${transaction.timestamp}</span><span class="text-xs">Reason: ${transaction.reason}</span></div></div><div class="d-flex align-items-center text-danger text-gradient text-sm font-weight-bold">- ${currencyFormatter.format(transaction.amount)}</div></li>`
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

const setAndDisplayModal = (markup) => {
    // set the modal
    document.getElementById("modal-container").innerHTML = markup

    // display the modal
    const modal = document.getElementById("modal");
    modal.style.display = "block"
    modal.style.opacity = "100"
    modal.style.background = "rgba(0,0,0,0.4)"
    modal.style.zIndex = "10000"

    return modal
}

const displayGenericModal = (title, message) => {
    const MODAL = `<div id="modal"  class="modal fade" tabindex="-1" role="dialog" aria-labelledby="modal-default" aria-hidden="true"><div class="modal-dialog modal- modal-dialog-centered modal-" role="document"><div class="modal-content"><div class="modal-header"><h6 class="modal-title" id="modal-title-default">${title}</h6></div><div class="modal-body">${message}</div><div class="modal-footer"><button id="close-modal-button" type="button" class="btn mb-0 bg-gradient-primary">Close</button></div></div></div></div>`
    const modal = setAndDisplayModal(MODAL)

    // close button
    modal.getElementsByTagName("button")[0].addEventListener("click", () => {
        document.getElementById("modal-container").innerHTML = ""
    })
}

const validateAccountCode = (code) => {
    let regex = /^\d+$/;
    code = code.trim().replaceAll(" ", "")
    if (regex.test(code) && code.length === 12) return true
    displayGenericModal("Invalid account number", "Please insert a valid beneficiary account number")
    return false
}

const validateAmount = (amount) => {
    amount = parseInt(amount)
    let balance = parseInt(document.getElementById("balance").textContent.replace(",", "").replace("$", "").trim())
    if (amount > 0 && amount <= balance) return true
    displayGenericModal("Invalid amount", "Please insert a valid amount that is more than zero and within your balance")
    return false
}

const autocomplete = (inputField, possibleValues) => {
    let currentFocus;

    // triggered when someone is writing in the field
    ["input", "focus", "click"].forEach(event => inputField.addEventListener(event, function () {
        // set values
        if (inputField.id === "destination-code") possibleValues = globalThis.usernamesAccountsInContacts[document.getElementById("beneficiary-username").value.trim()]

        // in case there are no possible values end
        if (possibleValues === undefined || possibleValues === null) return;

        let valuesListHtml, valueHtml, value = this.value;

        // close any already open lists of autocompleted values
        closeAllLists();

        currentFocus = -1;

        // create a DIV element that will contain the items (values)
        valuesListHtml = document.createElement("DIV");
        valuesListHtml.setAttribute("id", this.id + "-autocomplete-list");
        valuesListHtml.setAttribute("class", "autocomplete-items list-group text-left  position-absolute z-index-3 shadow-card text-sm");

        // append the DIV element as a child of the autocomplete container
        this.parentNode.appendChild(valuesListHtml);

        possibleValues.forEach(candidate => {
            // create a DIV element for each matching element
            if (value.trim() !== candidate.trim() && (value.length === 0 || candidate.slice(0, value.length).toUpperCase() === value.toUpperCase())) {
                valueHtml = document.createElement("DIV");
                valueHtml.setAttribute("class", "list-group-item list-group-item-action");
                valueHtml.innerHTML = "<strong>" + candidate.slice(0, value.length) + "</strong>";
                valueHtml.innerHTML += candidate.slice(value.length);
                valueHtml.innerHTML += "<input type='hidden' value='" + candidate + "'>";

                valueHtml.addEventListener("click", function () {
                    inputField.value = this.getElementsByTagName("input")[0].value;
                    autocomplete(document.getElementById("destination-code"), null);
                    closeAllLists();
                });

                valuesListHtml.appendChild(valueHtml);
            }
        })
    }));

    // execute when someone presses a key on the keyboard
    inputField.addEventListener("keydown", function (e) {
        if (document.getElementById(this.id + "-autocomplete-list").getElementsByTagName("div") === null) return;
        let elements = document.getElementById(this.id + "-autocomplete-list").getElementsByTagName("div");
        if (e.keyCode === 40) { // arrow down
            currentFocus++;
            addActive(elements); // make the current item more visible
        } else if (e.keyCode === 38) { // arrow up
            currentFocus--;
            addActive(elements);
        } else if (e.keyCode === 13) { // enter key
            e.preventDefault(); // prevent the form from being submitted
            if (currentFocus > -1) {
                if (elements) elements[currentFocus].click(); // simulate a click on the "active" item
            }
        }
    });

    let addActive = (elements) => {
        removeActive(elements);  // remove the "active" class on all items
        if (currentFocus >= elements.length) currentFocus = 0;
        if (currentFocus < 0) currentFocus = elements.length - 1;
        elements[currentFocus].classList.add("active");
    }

    let removeActive = (elements) => {
        for (let i = 0; i < elements.length; i++) {
            elements[i].classList.remove("active");
        }
    }

    let closeAllLists = (element) => {
        // prevent it from closing the list when click on input field
        if (element !== undefined && element.id === "beneficiary-username" && null !== document.getElementById("beneficiary-username-autocomplete-list")) return
        if (element !== undefined && element.id === "destination-code" && null !== document.getElementById("destination-code-autocomplete-list")) return

        // close all autocomplete lists in the document, except the one passed as an argument
        let elements = document.getElementsByClassName("autocomplete-items");
        for (let i = 0; i < elements.length; i++) {
            if (element !== elements[i] && element !== inputField) {
                elements[i].parentNode.removeChild(elements[i]);
            }
        }
    }

    //execute when someone clicks in the document
    document.addEventListener("click", function (e) {
        closeAllLists(e.target);
    });
};