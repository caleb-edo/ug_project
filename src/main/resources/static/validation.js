document.addEventListener("DOMContentLoaded", function () {
    const loginForm = document.getElementById("login-form");
    const registerForm = document.getElementById("register-form");

    if (loginForm) {
        loginForm.addEventListener("submit", function (event) {
            let username = document.getElementById("username").value.trim();
            let password = document.getElementById("password").value.trim();
            let isValid = true;

            if (username.length < 3 || username.length > 20) {
                alert("Username must be between 3 and 20 characters");
                isValid = false;
            }

            if (password.length < 6) {
                alert("Password must be at least 6 characters long");
                isValid = false;
            }

            if (!isValid) {
                event.preventDefault();
            }
        });
    }

    if (registerForm) {
        registerForm.addEventListener("submit", function (event) {
            let username = document.getElementById("username").value.trim();
            let password = document.getElementById("password").value.trim();
            let confirmPassword = document.getElementById("confirm-password").value.trim();
            let role = document.getElementById("role").value;
            let isValid = true;

            if (username.length < 3 || username.length > 20) {
                alert("Username must be between 3 and 20 characters");
                isValid = false;
            }

            if (password.length < 6) {
                alert("Password must be at least 6 characters long");
                isValid = false;
            }

            if (password !== confirmPassword) {
                alert("Passwords do not match");
                isValid = false;
            }

            if (role === "") {
                alert("Please select a role");
                isValid = false;
            }

            if (!isValid) {
                event.preventDefault();
            }
        });
    }
});
