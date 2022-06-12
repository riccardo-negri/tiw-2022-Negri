{ // avoid variables ending up in the global scope
    window.addEventListener("load", () => {
        if (localStorage.getItem("user") !== null) {
            window.location.href = "app.html";
        }
    })

    document.getElementById("registration-button").addEventListener('click', (e) => {
        const form = document.getElementById("form");
        e.preventDefault();
        if (form.checkValidity()) {
            if (validateEmail(form) && validatePasswords(form)) {
                makeCall("POST", 'registration', form, function (x) {
                    if (x.readyState === XMLHttpRequest.DONE) {
                        const message = x.responseText;
                        switch (x.status) {
                            case 200:
                                window.location.href = "index.html";
                                sessionStorage.setItem("registered", "yes")
                                break;
                            case 400: // bad request
                                document.getElementById("error-message").textContent = message;
                                break;
                            case 401: // unauthorized
                                document.getElementById("error-message").textContent = message;
                                break;
                            case 500: // server error
                                document.getElementById("error-message").textContent = message;
                                break;
                        }
                    }
                });
            }
        } else {
            form.reportValidity();
        }
    });

    function validateEmail(form) {
        let regex = /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/;
        if (regex.test(form.elements["email"].value)) {
            return true
        }
        alert("You have entered an invalid email address!")
        return false
    }

    function validatePasswords(form) {
        if (form.elements["password"].value === form.elements["password-control"].value) {
            return true
        }
        alert("You have entered two different passwords!")
        return false
    }
}