/**
 * Internship Application Tracker - Authentication Handler
 */

document.addEventListener('DOMContentLoaded', () => {
    // ----------------- Login Form -----------------
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const alertBox = document.getElementById('authAlert');
            const submitBtn = loginForm.querySelector('button[type="submit"]');

            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value;

            try {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Signing in...';
                showAlert(alertBox, '', 'd-none');

                await API.auth.login({ email, password });
                window.location.href = '/index.html';
            } catch (err) {
                showAlert(alertBox, err.message, 'alert-danger');
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="bi bi-box-arrow-in-right me-2"></i>Sign In';
            }
        });
    }

    // ----------------- Register Form -----------------
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const alertBox = document.getElementById('authAlert');
            const submitBtn = registerForm.querySelector('button[type="submit"]');

            const fullName = document.getElementById('fullName').value.trim();
            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (password !== confirmPassword) {
                showAlert(alertBox, 'Passwords do not match. Please re-enter.', 'alert-danger');
                return;
            }

            if (password.length < 6) {
                showAlert(alertBox, 'Password must be at least 6 characters long.', 'alert-danger');
                return;
            }

            try {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Creating account...';
                showAlert(alertBox, '', 'd-none');

                await API.auth.register({ fullName, email, password });

                showAlert(alertBox, 'Account created successfully! Logging you in...', 'alert-success');
                // Automatically log in newly registered user
                setTimeout(async () => {
                    await API.auth.login({ email, password });
                    window.location.href = '/index.html';
                }, 1000);
            } catch (err) {
                showAlert(alertBox, err.message, 'alert-danger');
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="bi bi-person-plus me-2"></i>Create Account';
            }
        });
    }

    function showAlert(box, msg, cssClass) {
        if (!box) return;
        box.className = `alert ${cssClass}`;
        box.textContent = msg;
        if (cssClass === 'd-none') {
            box.style.display = 'none';
        } else {
            box.style.display = 'block';
        }
    }
});
