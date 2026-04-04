// Using API_URL from config.js

document.addEventListener("DOMContentLoaded", () => {
    // Tabs switching
    const btnLogin = document.getElementById('tab-login');
    const btnRegister = document.getElementById('tab-register');
    const formLogin = document.getElementById('login-form');
    const formRegister = document.getElementById('register-form');

    if(btnLogin) {
        btnLogin.addEventListener('click', () => {
            btnLogin.classList.add('active');
            btnRegister.classList.remove('active');
            
            formLogin.classList.add('active');
            formLogin.classList.remove('hidden');
            
            formRegister.classList.remove('active');
            formRegister.classList.add('hidden');
        });
    }

    if(btnRegister) {
        btnRegister.addEventListener('click', () => {
            btnRegister.classList.add('active');
            btnLogin.classList.remove('active');
            
            formRegister.classList.add('active');
            formRegister.classList.remove('hidden');
            
            formLogin.classList.remove('active');
            formLogin.classList.add('hidden');
        });
    }

    // Handles form submissions
    if(formLogin) {
        formLogin.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('login-email').value;
            const password = document.getElementById('login-password').value;
            const errDiv = document.getElementById('login-error');
            
            try {
                const res = await fetch(`${API_URL}/users/login`, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({email, password})
                });
                
                const data = await res.json();
                
                if (res.ok) {
                    localStorage.setItem('token', data.token);
                    localStorage.setItem('userName', data.name);
                    window.location.href = 'dashboard.html';
                } else {
                    errDiv.style.display = 'block';
                    errDiv.textContent = data.message || "Login failed";
                }
            } catch (err) {
                console.error("Login Error:", err);
                errDiv.style.display = 'block';
                errDiv.textContent = "Network error. Make sure the backend server at " + BASE_URL + " is running.";
            }
        });
    }

    if(formRegister) {
        formRegister.addEventListener('submit', async (e) => {
            e.preventDefault();
            const name = document.getElementById('reg-name').value;
            const email = document.getElementById('reg-email').value;
            const password = document.getElementById('reg-password').value;
            const errDiv = document.getElementById('reg-error');
            
            try {
                const res = await fetch(`${API_URL}/users/register`, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({name, email, password})
                });
                
                const data = await res.json();
                
                if (res.ok) {
                    localStorage.setItem('token', data.token);
                    localStorage.setItem('userName', data.name);
                    window.location.href = 'dashboard.html';
                } else {
                    errDiv.style.display = 'block';
                    errDiv.textContent = data.message || "Registration failed";
                }
            } catch (err) {
                console.error("Registration Error:", err);
                errDiv.style.display = 'block';
                errDiv.textContent = "Network error. Make sure the backend server at " + BASE_URL + " is running.";
            }
        });
    }
});
