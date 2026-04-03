const API_URL = "http://localhost:8080/api/v1";

document.addEventListener("DOMContentLoaded", () => {
    
    // Auth Check
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    const userName = localStorage.getItem('userName');
    document.getElementById('user-greeting').textContent = `Welcome, ${userName || 'User'}`;

    // Logout
    document.getElementById('btn-logout').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = 'index.html';
    });

    // File Upload Drag & Drop
    const dropZone = document.getElementById('drop-zone');
    const fileInput = document.getElementById('file-input');
    const btnAnalyze = document.getElementById('btn-analyze');
    const uploadStatus = document.getElementById('upload-status');
    let selectedFile = null;

    dropZone.addEventListener('click', () => fileInput.click());
    
    dropZone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropZone.style.background = 'rgba(59, 130, 246, 0.2)';
    });

    dropZone.addEventListener('dragleave', (e) => {
        e.preventDefault();
        dropZone.style.background = 'rgba(59, 130, 246, 0.05)';
    });

    dropZone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropZone.style.background = 'rgba(59, 130, 246, 0.05)';
        if (e.dataTransfer.files.length) {
            handleFileSelect(e.dataTransfer.files[0]);
        }
    });

    fileInput.addEventListener('change', () => {
        if (fileInput.files.length) handleFileSelect(fileInput.files[0]);
    });

    function handleFileSelect(file) {
        if (file.type !== "application/pdf") {
            uploadStatus.innerHTML = '<span style="color:var(--danger)">Please select a PDF file.</span>';
            selectedFile = null;
            btnAnalyze.disabled = true;
            return;
        }
        if (file.size > 10 * 1024 * 1024) {
            uploadStatus.innerHTML = '<span style="color:var(--danger)">File size exceeds 10MB limit.</span>';
            selectedFile = null;
            btnAnalyze.disabled = true;
            return;
        }
        
        selectedFile = file;
        uploadStatus.innerHTML = `<span style="color:var(--success)">Selected: ${file.name}</span>`;
        btnAnalyze.disabled = false;
    }

    // Process Analysis
    let currentResumeId = null;

    btnAnalyze.addEventListener('click', async () => {
        if (!selectedFile) return;

        btnAnalyze.disabled = true;
        btnAnalyze.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Analyzing...';
        
        const token = localStorage.getItem('token');
        if (!token || token === "null" || token === "undefined") {
            uploadStatus.innerHTML = '<span style="color:var(--danger)">Session expired. Please logout and login again.</span>';
            btnAnalyze.disabled = false;
            btnAnalyze.innerHTML = 'Analyze Resume';
            return;
        }

        const formData = new FormData();
        formData.append("file", selectedFile);

        try {
            console.log("Starting upload with token:", token.substring(0, 10) + "...");
            const res = await fetch(`${API_URL}/resumes/upload`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            if (res.status === 403) {
                localStorage.removeItem('token');
                throw new Error("Session invalid (403). Please register/login again.");
            }

            let data;
            const contentType = res.headers.get("content-type");
            if (contentType && contentType.indexOf("application/json") !== -1) {
                data = await res.json();
            }

            if (res.ok) {
                if (!data) throw new Error("Server returned success but no data");
                currentResumeId = data.resumeId;
                fetchAnalysisDetails(currentResumeId);
            } else {
                throw new Error(data?.message || `Upload failed with status ${res.status}`);
            }
        } catch (err) {
            console.error("Upload error details:", err);
            uploadStatus.innerHTML = `<span style="color:var(--danger)">${err.message}</span>`;
            btnAnalyze.disabled = false;
            btnAnalyze.innerHTML = 'Analyze Resume';
            
            if (err.message.includes("403")) {
                setTimeout(() => window.location.href = 'index.html', 2000);
            }
        }
    });

    async function fetchAnalysisDetails(resumeId) {
        try {
            const res = await fetch(`${API_URL}/resumes/${resumeId}/analysis`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            let data;
            const contentType = res.headers.get("content-type");
            if (contentType && contentType.indexOf("application/json") !== -1) {
                data = await res.json();
            }

            if (res.ok) {
                if (!data) throw new Error("Server returned success but no analysis data");
                renderAnalysisResults(data);
            }
        } catch (err) {
            console.error("Failed to fetch analysis details", err);
        } finally {
            btnAnalyze.disabled = false;
            btnAnalyze.innerHTML = 'Analyze Resume';
        }
    }

    function renderAnalysisResults(data) {
        document.getElementById('results-section').classList.remove('hidden');

        // Score Gauge
        const scoreText = document.getElementById('score-text');
        const scoreCirclePath = document.getElementById('score-circle-path');
        
        // Animate score from 0 to actual
        let currentScore = 0;
        const targetScore = data.score;
        const interval = setInterval(() => {
            if (currentScore >= targetScore) {
                clearInterval(interval);
            } else {
                currentScore++;
                scoreText.textContent = `${currentScore}%`;
                scoreCirclePath.setAttribute('stroke-dasharray', `${currentScore}, 100`);
                
                // Color based on score
                if (currentScore < 40) scoreCirclePath.style.stroke = "var(--danger)";
                else if (currentScore < 70) scoreCirclePath.style.stroke = "var(--warning)";
                else scoreCirclePath.style.stroke = "var(--success)";
            }
        }, 15);

        // Category Bars
        const barsContainer = document.getElementById('category-bars-container');
        barsContainer.innerHTML = '';
        if (data.categoryScores) {
            Object.entries(data.categoryScores).forEach(([cat, s]) => {
                const html = `
                <div class="bar-container">
                    <div class="bar-label"><span>${cat}</span><span>${s}%</span></div>
                    <div class="bar-bg"><div class="bar-fill" style="width: 0%" data-target="${s}%"></div></div>
                </div>`;
                barsContainer.insertAdjacentHTML('beforeend', html);
            });
            // Animate bars
            setTimeout(() => {
                document.querySelectorAll('.bar-fill').forEach(bar => {
                    bar.style.width = bar.getAttribute('data-target');
                    // Color code
                    const val = parseInt(bar.getAttribute('data-target'));
                    if (val < 40) bar.style.background = "var(--danger)";
                    else if (val < 70) bar.style.background = "var(--warning)";
                    else bar.style.background = "var(--success)";
                });
            }, 100);
        }

        // Matched Skills
        const matchedContainer = document.getElementById('matched-chips');
        matchedContainer.innerHTML = '';
        data.matchedSkills.forEach(skill => {
            matchedContainer.insertAdjacentHTML('beforeend', `<span class="chip matched">${skill}</span>`);
        });

        // Missing Skills
        const missingContainer = document.getElementById('missing-chips');
        missingContainer.innerHTML = '';
        data.missingSkills.forEach(skill => {
            missingContainer.insertAdjacentHTML('beforeend', `<span class="chip missing">${skill}</span>`);
        });

        // Suggestions
        const suggContainer = document.getElementById('suggestions-list');
        suggContainer.innerHTML = '';
        data.suggestions.forEach(s => {
            suggContainer.insertAdjacentHTML('beforeend', `<li>${s}</li>`);
        });
        
        // Let chat know analysis is complete (simulate AI opening message)
        addChatMessage("ai", `I've analyzed your resume and scored it ${data.score}/100. Let me know if you want to know how to improve or what skills to add!`);
    }

    // AI Chat Widget
    const btnToggleChat = document.getElementById('btn-toggle-chat');
    const closeChat = document.getElementById('close-chat');
    const chatWidget = document.getElementById('chat-widget');
    const sendChatBtn = document.getElementById('send-chat');
    const chatInput = document.getElementById('chat-input');
    const chatMsgs = document.getElementById('chat-msgs');

    btnToggleChat.addEventListener('click', () => {
        chatWidget.classList.add('active');
        btnToggleChat.style.transform = 'scale(0)';
    });

    closeChat.addEventListener('click', () => {
        chatWidget.classList.remove('active');
        btnToggleChat.style.transform = 'scale(1)';
    });

    sendChatBtn.addEventListener('click', handleSendChat);
    chatInput.addEventListener('keypress', (e) => {
        if(e.key === 'Enter') handleSendChat();
    });

    async function handleSendChat() {
        const msg = chatInput.value.trim();
        if (!msg) return;

        addChatMessage('user', msg);
        chatInput.value = '';

        // Add loading indicator
        const loadingId = addChatLoading();
        scrollToBottom();

        try {
            const res = await fetch(`${API_URL}/chat`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    message: msg,
                    resumeId: currentResumeId
                })
            });
            
            removeElement(loadingId);

            if (res.ok) {
                const data = await res.json();
                addChatMessage('ai', data.reply);
            } else {
                addChatMessage('ai', "Sorry, I am having trouble connecting to the server.");
            }
        } catch (err) {
            removeElement(loadingId);
            addChatMessage('ai', "Hmm, looks like there's a network issue.");
        }
        scrollToBottom();
    }

    function addChatMessage(sender, text) {
        const div = document.createElement('div');
        div.className = `msg ${sender}`;
        div.textContent = text;
        chatMsgs.appendChild(div);
        scrollToBottom();
    }

    function addChatLoading() {
        const id = 'loading-' + Date.now();
        const html = `<div id="${id}" class="msg ai typing">
                        <div class="dot"></div><div class="dot"></div><div class="dot"></div>
                     </div>`;
        chatMsgs.insertAdjacentHTML('beforeend', html);
        return id;
    }

    function removeElement(id) {
        const el = document.getElementById(id);
        if (el) el.remove();
    }

    function scrollToBottom() {
        chatMsgs.scrollTop = chatMsgs.scrollHeight;
    }
});
