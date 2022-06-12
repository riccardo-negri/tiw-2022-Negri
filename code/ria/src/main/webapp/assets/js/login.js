(function () { // avoid variables ending up in the global scope
    if (sessionStorage.getItem("registered") === "yes") document.getElementById("registered-message").textContent = "Registered successfully!";

    document.getElementById("login-button").addEventListener('click', (e) => {
        const form = e.target.closest("form");

        if (form.checkValidity()) {
            makeCall("POST", 'login', e.target.closest("form"),
                function (x) {
                    if (x.readyState === XMLHttpRequest.DONE) {
                        const message = x.responseText;
                        switch (x.status) {
                            case 200:
                                let user = message
                                sessionStorage.setItem('user', user);
                                window.location.href = "app.html";
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
                }
            );
        } else {
            form.reportValidity();
        }
    });

})();