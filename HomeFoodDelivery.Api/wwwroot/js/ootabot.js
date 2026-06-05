
// Automatically inject OotaBot into any page
(function () {
    const init = () => {
        if (document.getElementById('ootabot-container')) return;

        // --- (Paste the full botHTML code here from the previous step) ---

        initOotaBot(); // Call the function defined below
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();

function initOotaBot() {
    if (document.getElementById('ootabot-container')) return;

    const botHTML = `
        <div id="ootabot-container" style="position: fixed; bottom: 20px; right: 20px; z-index: 9999;">
            <!-- Chat Window -->
            <div id="ootabot-window" class="card shadow-lg border-0 rounded-4 d-none mb-3" style="width: 300px; display: none;">
                <div class="card-header bg-primary text-white rounded-top-4 d-flex justify-content-between align-items-center p-3">
                    <span class="fw-bold">OotaBot</span>
                    <button type="button" class="btn-close btn-close-white" onclick="toggleBot()"></button>
                </div>
                <div class="card-body" style="height: 250px; overflow-y: auto; background-color: #f8f9fa;" id="botMessages">
                    <div class="d-flex mb-3">
                        <div class="bg-white p-2 rounded-3 shadow-sm text-dark small border">Hi! Need help?</div>
                    </div>
                </div>
                <div class="card-footer bg-white border-top p-2 rounded-bottom-4">
                    <div class="d-flex flex-column gap-2">
                        <button class="btn btn-sm btn-outline-primary fw-bold text-start rounded-pill px-3" onclick="askBot('order')">Where is my order?</button>
                        <button class="btn btn-sm btn-outline-primary fw-bold text-start rounded-pill px-3" onclick="askBot('wallet')">Check wallet balance</button>
                    </div>
                </div>
            </div>
            <!-- Floating Button -->
            <button id="ootabot-btn" class="btn btn-primary rounded-circle shadow-lg d-flex justify-content-center align-items-center"
                style="width: 60px; height: 60px; padding: 0;"
                onclick="toggleBot()">
                <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                </svg>
            </button>
        </div>`;


    document.body.insertAdjacentHTML('beforeend', botHTML);
}

window.toggleBot = function () {
    const win = document.getElementById('ootabot-window');
    const btn = document.getElementById('ootabot-btn');

    if (win.style.display === 'none') {
        win.style.display = 'block';
        win.classList.remove('d-none');
        btn.classList.add('d-none');
    } else {
        win.style.display = 'none';
        win.classList.add('d-none');
        btn.classList.remove('d-none');
    }
};

window.askBot = async function (intent) {
    const userId = localStorage.getItem('userId');
    const msgs = document.getElementById('botMessages');

    if (intent === 'order') {
        msgs.innerHTML += `<div class="d-flex mb-3 justify-content-end"><div class="bg-primary text-white p-2 rounded-3 small">Where is my order?</div></div>`;
        const res = await fetch(`/api/Orders/customer/${userId}`);
        const orders = await res.json();

        if (orders.length === 0) {
            msgs.innerHTML += `<div class="d-flex mb-3"><div class="bg-white p-2 rounded-3 small border">No active orders found.</div></div>`;
        } else {
            const latest = orders[0];
            msgs.innerHTML += `<div class="d-flex mb-3"><div class="bg-white p-2 rounded-3 small border">Latest order status: <b>${latest.orderStatus}</b></div></div>`;
        }
    } else if (intent === 'wallet') {
        msgs.innerHTML += `<div class="d-flex mb-3 justify-content-end"><div class="bg-primary text-white p-2 rounded-3 small">Check wallet balance</div></div>`;
        const res = await fetch(`/api/Wallets/${userId}`);
        const wallet = await res.json();
        msgs.innerHTML += `<div class="d-flex mb-3"><div class="bg-white p-2 rounded-3 small border">Your balance is <b>₹${wallet.balance.toFixed(2)}</b></div></div>`;
    }
    msgs.scrollTop = msgs.scrollHeight;
};

document.addEventListener('DOMContentLoaded', initOotaBot);