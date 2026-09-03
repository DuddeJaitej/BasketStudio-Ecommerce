const state = {
    products: [],
    cart: JSON.parse(localStorage.getItem('basket') || '{}'),
    token: localStorage.getItem('basketToken') || '',
    pendingCheckout: false
};
const $ = s => document.querySelector(s);
const money = n => new Intl.NumberFormat('en-IN').format(n);
async function loadProducts(category = 'All') {
    const response = await fetch('/api/products' + (category === 'All' ? '' : `?category=${category}`));
    if (!response.ok) throw new Error('Catalog request failed');
    state.products = await response.json();
    renderProducts()
}
function renderProducts() {
    const grid = $('#productGrid');
    grid.innerHTML = state.products.map((p, i) => `<article class="product-card" style="animation-delay:${i * 35}ms">
    <div class="product-image"><img src="/assets/${p.image}" alt="${p.name}" onerror="this.style.display='none';this.nextElementSibling.style.display='block'">
    <span class="fallback" style="display:none">
        ${p.category === 'Fruits' ? '🍊' : '🥬'}
    </span></div><div class="product-meta"><h3>${p.name}</h3><p>${p.tagline} · 1 kg</p><div class="product-bottom"><span class="price">₹${money(p.price)}</span><button class="add" data-add="${p.id}" aria-label="Add ${p.name}">+</button></div></div></article>`).join('')
}
function cartEntries() {
    return Object.entries(state.cart).map(([id, quantity]) => ({
        product: state.products.find(p => p.id === id) || allProducts.find(p => p.id === id), quantity
    })).filter(x => x.product)
}
let allProducts = [];
function renderCart() {
    const entries = cartEntries();
    const count = entries.reduce((sum, x) => sum + x.quantity, 0);
    const total = entries.reduce((sum, x) => sum + x.product.price * x.quantity, 0);
    $('#cartCount').textContent = count;
    $('#cartTotal').textContent = money(total);
    $('#basketItems').innerHTML = entries.length ? entries.map(({ product, quantity }) => `<div class="drawer-item"><img src="/assets/${product.image}" alt="${product.name}"><div><h3>${product.name}</h3><p>₹${money(product.price)} · 1 kg</p><div class="quantity"><button data-minus="${product.id}">−</button><span>${quantity}</span><button data-plus="${product.id}">+</button></div></div><button class="remove" data-remove="${product.id}" aria-label="Remove">×</button></div>`).join('') : '<p class="empty">Your basket is waiting for something lovely.</p>'; localStorage.setItem('basket', JSON.stringify(state.cart))
}
function toast(message) {
    const item = $('#toast'); item.textContent = message;
    item.classList.add('show');
    setTimeout(() => item.classList.remove('show'), 2200)
}
function openModal(content) {
    $('#modalContent').innerHTML = content;
    $('#modal').classList.add('open')
}
function authModal() {
    openModal(`<p class="eyebrow">Welcome back</p><h2>Make it personal.</h2><form id="authForm"><div class="field"><label>Email</label><input name="email" type="email" required></div><div class="field"><label>Password</label><input name="password" type="password" minlength="8" required></div><p class="error" id="authError"></p><button class="primary-button full">Sign in <span>→</span></button></form><p style="font-size:12px;color:var(--muted);margin-top:18px">New here? <a href="#" id="signupLink" style="color:var(--green)">Create an account</a></p>`);
    $('#authForm').onsubmit = async e => {
        e.preventDefault();
        const data = Object.fromEntries(new FormData(e.target));
        const r = await fetch('/api/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
        if (!r.ok) {
            $('#authError').textContent = 'Could not sign in with those details.';
            return
        }
        const user = await r.json();
        state.token = user.token;
        localStorage.setItem('basketToken', state.token);
        $('#modal').classList.remove('open');
        toast(`Welcome back, ${user.name.split(' ')[0]}.`)
    };
    $('#signupLink').onclick = e => {
        e.preventDefault(); signupModal()
    }
}
function signupModal() {
    openModal(`<p class="eyebrow">Join the studio</p><h2>Your best basket yet.</h2><form id="signupForm"><div class="form-grid"><div class="field"><label>Name</label><input name="name" autocomplete="name" required></div><div class="field"><label>Phone</label><input name="phone" type="tel" autocomplete="tel" pattern="[0-9]{10}" required></div></div><div class="field"><label>Email</label><input name="email" type="email" autocomplete="email" required></div><div class="form-grid"><div class="field"><label>Password</label><input name="password" type="password" minlength="8" autocomplete="new-password" required></div><div class="field"><label>Date of birth</label><input name="dateOfBirth" type="date" max="${new Date().toISOString().slice(0, 10)}" required></div></div><p class="error" id="signupError"></p><button class="primary-button full">Create account <span>→</span></button></form>`);
    $('#signupForm').onsubmit = async e => {
        e.preventDefault();
        const data = Object.fromEntries(new FormData(e.target));
        const error = $('#signupError');
        const button = e.target.querySelector('button');
        button.disabled = true;
        error.textContent = '';
        try {
            const r = await fetch('/api/auth/signup', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
            if (!r.ok) {
                const problem = await r.json().catch(() => ({}));
                error.textContent = r.status === 409
                    ? 'An account already exists for this email.'
                    : problem.detail || problem.message || 'Please check your details and try again.';
                return
            }
            const user = await r.json();
            state.token = user.token;
            localStorage.setItem('basketToken', state.token);
            $('#modal').classList.remove('open');
            toast('Your studio account is ready.')
        } catch (requestError) {
            error.textContent = 'Could not connect to the account service. Please try again.';
        } finally {
            button.disabled = false;
        }
    }
}
function checkout() {
    if (!Object.keys(state.cart).length) {
        toast('Add something to your basket first.'); return
    } if (!state.token) {
        authModal(); toast('Sign in to complete your order.'); return
    } openModal(`<p class="eyebrow">Almost home</p><h2>Delivery details.</h2><form id="checkoutForm"><div class="form-grid"><div class="field"><label>Name</label><input name="name" required></div><div class="field"><label>Phone</label><input name="phone" required></div></div><div class="field"><label>Address</label><input name="line" placeholder="Flat, building, street" required></div><div class="form-grid"><div class="field"><label>City</label><input name="city" required></div><div class="field"><label>State</label><input name="state" required></div></div><div class="form-grid"><div class="field"><label>PIN code</label><input name="pincode" pattern="[0-9]{6}" required></div><div class="field"><label>Payment</label><select name="paymentMethod"><option>Cash on delivery</option><option>UPI</option><option>Card</option></select></div></div><p class="error" id="checkoutError"></p><button class="primary-button full">Place order <span>→</span></button></form>`); $('#checkoutForm').onsubmit = async e => { e.preventDefault(); const data = Object.fromEntries(new FormData(e.target)); const address = { name: data.name, phone: data.phone, line: data.line, city: data.city, state: data.state, pincode: data.pincode }; const r = await fetch('/api/orders', { method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${state.token}` }, body: JSON.stringify({ items: cartEntries().map(x => ({ productId: x.product.id, quantity: x.quantity })), address, paymentMethod: data.paymentMethod }) }); if (!r.ok) { $('#checkoutError').textContent = 'We could not place that order. Please try again.'; return } const order = await r.json(); state.cart = {}; renderCart(); $('#basketDrawer').classList.remove('open'); $('#drawerBackdrop').classList.remove('open'); openModal(`<div class="order-success"><div class="success-mark">✓</div><p class="eyebrow">It is on its way</p><h2>Order confirmed.</h2><p>Your order <strong>#${order.id}</strong> is now safely in your profile.</p><a class="primary-button full" href="/profile.html">View order details <span>→</span></a></div>`); }
}
document.addEventListener('click', e => {
    const add = e.target.closest('[data-add]');
    if (add) {
        state.cart[add.dataset.add] = (state.cart[add.dataset.add] || 0) + 1; renderCart();
        toast('Added to your basket.')
    }
    const plus = e.target.closest('[data-plus]');
    if (plus) {
        state.cart[plus.dataset.plus]++;
        renderCart()
    }
    const minus = e.target.closest('[data-minus]');
    if (minus) {
        state.cart[minus.dataset.minus]--;
        if (state.cart[minus.dataset.minus] <= 0) delete state.cart[minus.dataset.minus];
        renderCart()
    }
    const remove = e.target.closest('[data-remove]');
    if (remove) {
        delete state.cart[remove.dataset.remove];
        renderCart()
    }
    const filter = e.target.closest('.filter');
    if (filter) {
        document.querySelectorAll('.filter').forEach(x => x.classList.remove('active'));
        filter.classList.add('active');
        loadProducts(filter.dataset.category)
    }
});
$('#cartButton').onclick = () => {
    $('#basketDrawer').classList.add('open');
    $('#drawerBackdrop').classList.add('open')
};
$('#closeCart').onclick = $('#drawerBackdrop').onclick = () => {
    $('#basketDrawer').classList.remove('open');
    $('#drawerBackdrop').classList.remove('open')
};
$('#closeModal').onclick = () => $('#modal').classList.remove('open');
$('#checkoutButton').onclick = checkout;
const profileButton = $('#profileButton');
const profileDropdown = $('#profileDropdown');
profileButton.onclick = async event => {
    event.stopPropagation();
    const open = profileDropdown.classList.toggle('open');
    profileButton.setAttribute('aria-expanded', open);
    if (open) {
        if (!state.token) {
            $('#profileEmail').textContent = 'Sign in to view your account';
            return
        }
        const response = await fetch('/api/profile', {
            headers: { Authorization: `Bearer ${state.token}` }
        });
        if (response.ok) {
            const profile = await response.json();
            $('#profileEmail').textContent = profile.email
        }
        else {
            $('#profileEmail').textContent = 'Session expired'
        }
    }
};
document.addEventListener('click', () => {
    profileDropdown.classList.remove('open');
    profileButton.setAttribute('aria-expanded', 'false')
});
$('#logoutButton').onclick = async () => {
    if (state.token) await fetch('/api/auth/logout', {
        method: 'POST', headers: { Authorization: `Bearer ${state.token}` }
    });
    state.token = '';
    localStorage.removeItem('basketToken');
    profileDropdown.classList.remove('open');
    toast('You have been logged out.');
    setTimeout(() => location.assign('/'), 500)
};
(async () => {
    try {
        const response = await fetch('/api/products');
        if (!response.ok) throw new Error('Catalog request failed');
        allProducts = await response.json();
        state.products = allProducts;
        renderProducts();
        renderCart();
        const category = new URLSearchParams(location.search).get('category');
        if (category === 'Fruits' || category === 'Vegetables') {
            $(`.filter[data-category="${category}"]`).click()
        }
        if (location.hash === '#account') authModal();
        if (location.hash === '#basket') {
            $('#cartButton').click()
        }
        if (location.hash === '#checkout') checkout()
    } catch (error) {
        $('#productGrid').innerHTML = '<p class="empty">The harvest is taking a moment. Please refresh to try again.</p>';
        console.error(error)
    }
})();
const originalCheckout = checkout; checkout = () => {
    if (!state.token) state.pendingCheckout = true;
    originalCheckout();
};
$('#checkoutButton').onclick = checkout;
const originalAuthModal = authModal;
authModal = () => {
    originalAuthModal();
    const form = $('#authForm');
    if (form) form.addEventListener('submit', () => setTimeout(() => {
        if (state.token && state.pendingCheckout) {
            state.pendingCheckout = false; checkout()
        }
    }, 350), {
        once: true
    })
};
checkout = () => { if (!Object.keys(state.cart).length) { toast('Add something to your basket first.'); return } if (!state.token) { state.pendingCheckout = true; authModal(); return } const entries = cartEntries(); openModal(`<p class="eyebrow">Almost home</p><h2>Delivery details.</h2><form id="checkoutForm"><div class="form-grid"><div class="field"><label>Name</label><input name="name" autocomplete="name" required></div><div class="field"><label>Phone</label><input name="phone" autocomplete="tel" pattern="[0-9]{10}" required></div></div><div class="field"><label>Address</label><input name="line" autocomplete="street-address" placeholder="Flat, building, street" required></div><div class="form-grid"><div class="field"><label>City</label><input name="city" required></div><div class="field"><label>State</label><input name="state" required></div></div><div class="form-grid"><div class="field"><label>PIN code</label><input name="pincode" inputmode="numeric" pattern="[0-9]{6}" required></div><div class="field"><label>Payment</label><select name="paymentMethod"><option>Cash on delivery</option><option>UPI</option><option>Card</option></select></div></div><p class="error" id="checkoutError"></p><button class="primary-button full">Place order <span>→</span></button></form>`); $('#checkoutForm').onsubmit = async event => { event.preventDefault(); const data = Object.fromEntries(new FormData(event.target)); const address = { name: data.name, phone: data.phone, line: data.line, city: data.city, state: data.state, pincode: data.pincode }; const response = await fetch('/api/orders', { method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${state.token}` }, body: JSON.stringify({ items: entries.map(item => ({ productId: item.product.id, quantity: item.quantity })), address, paymentMethod: data.paymentMethod }) }); if (!response.ok) { $('#checkoutError').textContent = 'We could not place that order. Please check your details and try again.'; return } const order = await response.json(); const total = entries.reduce((sum, item) => sum + item.product.price * item.quantity, 0); state.cart = {}; renderCart(); $('#basketDrawer').classList.remove('open'); $('#drawerBackdrop').classList.remove('open'); openModal(`<div class="order-success"><div class="success-mark">✓</div><p class="eyebrow">It is on its way</p><h2>Order confirmed.</h2><p>Your order <strong>#${order.id}</strong> has been placed successfully.</p><div class="confirmation-items">${entries.map(item => `<img src="/assets/${item.product.image}" alt="${item.product.name}">`).join('')}</div><div class="confirmation-total">Total paid <strong>₹${money(total)}</strong></div><a class="primary-button full" href="/profile.html">View order details <span>→</span></a></div>`) }; }; $('#checkoutButton').onclick = checkout;
