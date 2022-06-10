{ // avoid variables ending up in the global scope

    // page components
    let homeView = new HomeView(); // main controller

    window.addEventListener("load", () => {
        if (sessionStorage.getItem("username") == null) {
            window.location.href = "index.html";
        } else {
            homeView.show();
        }
    }, false);

    function HomeView() {
        this.show = function () {
            makeCall("GET", "get-account-list", null,
                function (req) {
                    if (req.readyState === 4) {
                        if (req.status === 200) {
                            const accounts = JSON.parse(req.responseText);
                            accounts.forEach(account => {
                                let markup = `<tr><td><div class="d-flex px-2 py-1"><div><img src="assets/img/logo_black.png" class="avatar avatar-sm me-3"alt="user1"></div><div class="d-flex flex-column justify-content-center"><h6 id="account-number" class="mb-0 text-sm">${account.code}</h6></div></div></td><td class="align-middle text-center"><p class="text-sm font-weight-bold mb-0">${account.balance}</p></td><td class="align-middle text-center"><span class="text-secondary text-xs font-weight-bold">${account.lastActivity}</span></td><td class="align-middle text-center"><a account="${account.id}" class="cursor-pointer text-primary font-weight-bold text-xs">VIEW</a></td></tr>`
                                document.getElementById("account-list").innerHTML += markup;
                            })


                            let anchors = document.getElementById("account-list").getElementsByTagName("a")
                            for (let anchor of anchors) {
                                anchor.addEventListener("click", (e) => {
                                    // TODO
                                    console.log("HERE" + e.target.getAttribute("account"))
                                }, false);
                            }
                        } else if (req.status === 403) {
                            // TODO
                        } else {
                            // TODO
                        }
                    }
                }
            );

        }
    }
}