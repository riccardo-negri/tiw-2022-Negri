// check if user saved locally is still valid
if (localStorage.getItem("user") !== null) {
    // make call to login page to see if I'm already logged in also from the server side
    makeCall("POST", 'login', null, function (x) {
        if (x.readyState === XMLHttpRequest.DONE) {
            switch (x.status) {
                case 400: // authorized (we passed the filter check), so either the JSESSION cookie is expired or not present
                    localStorage.removeItem("user")
                    document.location.href = "index.html"
                    break;
                default:
                    break;
            }
        }
    });
}

// check every 100 milliseconds if the user is logged in or logged out (some other tabs might have logged out or logged in)
setInterval(function () {
    if (-1 === document.location.href.indexOf("app.html") && localStorage.getItem("user") !== null) {
        document.location.href = "app.html"
    } else if (!(-1 === document.location.href.indexOf("app.html")) && localStorage.getItem("user") === null) {
        document.location.href = "index.html"
    }
}, 100);

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