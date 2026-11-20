const API_BASE = '/api';

async function api(path, options = {}) {
    const config = {
        headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
        ...options
    };
    const response = await fetch(`${API_BASE}${path}`, config);
    let data = null;
    const text = await response.text();
    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = { message: text };
        }
    }
    if (!response.ok) {
        const msg = (data && (data.error || data.message)) || `Request failed (${response.status})`;
        throw new Error(msg);
    }
    return data;
}

function showMessage(el, text, type = 'success') {
    if (!el) return;
    el.textContent = text;
    el.className = `feedback-msg ${type}`;
    el.hidden = false;
}

function clearMessage(el) {
    if (!el) return;
    el.hidden = true;
    el.textContent = '';
}

function escapeHtml(value) {
    if (value === null || value === undefined) return '';
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

async function loadCustomers() {
    const select = document.getElementById('existingCustomer');
    const customers = await api('/customers');
    const currentVal = select.value;
    select.innerHTML = '<option value="">-- Register as new customer --</option>' +
        customers.map(c => `<option value="${c.id}">${escapeHtml(c.name)} (${escapeHtml(c.contactNumber)})</option>`).join('');
    if (currentVal) select.value = currentVal;
    document.getElementById('statCustomers').textContent = customers.length;
    return customers;
}

async function loadVehicles(keyword = '') {
    const tbody = document.getElementById('vehicleTableBody');
    const orderSelect = document.getElementById('orderVehicleId');
    const dirMsg = document.getElementById('directoryMessage');
    clearMessage(dirMsg);

    try {
        const vehicles = await api(
            keyword ? `/vehicles/search?keyword=${encodeURIComponent(keyword)}` : '/vehicles'
        );

        if (!keyword) {
            document.getElementById('statVehicles').textContent = vehicles.length;
            const prevOrderVehicle = orderSelect.value;
            orderSelect.innerHTML = '<option value="">-- Select a registered vehicle --</option>' +
                vehicles.map(v =>
                    `<option value="${v.id}">${escapeHtml(v.registrationNumber)} - ${escapeHtml(v.make)} ${escapeHtml(v.model)}</option>`
                ).join('');
            if (prevOrderVehicle) orderSelect.value = prevOrderVehicle;
        }

        if (vehicles.length === 0) {
            tbody.innerHTML = `<tr><td colspan="9" class="empty-cell">${keyword ? 'No matching vehicles found.' : 'No vehicles registered yet.'}</td></tr>`;
            return vehicles;
        }

        tbody.innerHTML = vehicles.map(v => `
            <tr>
                <td><span class="reg-badge">${escapeHtml(v.registrationNumber)}</span></td>
                <td>${escapeHtml(v.chassisNumber)}</td>
                <td>${escapeHtml(v.make)} ${escapeHtml(v.model)}</td>
                <td>${v.manufactureYear || '-'}</td>
                <td>${escapeHtml(v.vehicleType || '-')}</td>
                <td>${v.customer ? escapeHtml(v.customer.name) : '-'}</td>
                <td>${v.customer ? escapeHtml(v.customer.contactNumber) : '-'}</td>
                <td>${escapeHtml(v.registrationDate || '-')}</td>
                <td>
                    <button type="button" class="btn-danger-sm" onclick="deleteVehicle(${v.id})">DELETE</button>
                </td>
            </tr>
        `).join('');
        return vehicles;
    } catch (err) {
        showMessage(dirMsg, `Error: ${err.message}`, 'error');
        return [];
    }
}

async function loadOrders() {
    const tbody = document.getElementById('orderTableBody');
    const orders = await api('/orders');
    document.getElementById('statOrders').textContent = orders.length;

    if (orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-cell">No orders yet.</td></tr>';
        return orders;
    }

    tbody.innerHTML = orders.map(o => `
        <tr>
            <td>${o.id}</td>
            <td><span class="reg-badge">${o.vehicle ? escapeHtml(o.vehicle.registrationNumber) : '-'}</span></td>
            <td>${escapeHtml(o.partName)}</td>
            <td>${o.quantity}</td>
            <td>${o.unitPrice != null ? Number(o.unitPrice).toFixed(2) : '0.00'}</td>
            <td><span class="status-badge">${escapeHtml(o.status || 'ORDERED')}</span></td>
            <td>${escapeHtml(o.orderDate || '-')}</td>
        </tr>
    `).join('');
    return orders;
}

window.deleteVehicle = async function (id) {
    const dirMsg = document.getElementById('directoryMessage');
    try {
        await api(`/vehicles/${id}`, { method: 'DELETE' });
        await refreshAll();
        showMessage(dirMsg, 'Vehicle record deleted.', 'success');
    } catch (err) {
        showMessage(dirMsg, err.message, 'error');
    }
};

async function refreshAll() {
    await Promise.all([loadCustomers(), loadVehicles(), loadOrders()]);
}

document.addEventListener('DOMContentLoaded', () => {
    // Tab switching
    const tabBtns = document.querySelectorAll('.tab-btn');
    const tabPanels = document.querySelectorAll('.tab-panel');
    tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            tabBtns.forEach(b => b.classList.remove('active'));
            tabPanels.forEach(p => p.classList.remove('active'));
            btn.classList.add('active');
            document.getElementById(btn.dataset.tab).classList.add('active');
        });
    });

    // Existing customer selection toggle
    const existingCustomerSelect = document.getElementById('existingCustomer');
    const customerFields = ['customerName', 'customerContact', 'customerEmail', 'customerAddress']
        .map(id => document.getElementById(id));

    existingCustomerSelect.addEventListener('change', () => {
        const hasExisting = Boolean(existingCustomerSelect.value);
        customerFields.forEach(field => {
            field.disabled = hasExisting;
            if (hasExisting) {
                field.removeAttribute('required');
            }
        });
        if (!hasExisting) {
            document.getElementById('customerName').setAttribute('required', 'required');
            document.getElementById('customerContact').setAttribute('required', 'required');
        }
    });

    // Vehicle registration form
    const regForm = document.getElementById('registrationForm');
    const regMsg = document.getElementById('regMessage');

    regForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearMessage(regMsg);

        const customerIdVal = existingCustomerSelect.value;
        const yearVal = document.getElementById('manufactureYear').value;

        const payload = {
            registrationNumber: document.getElementById('registrationNumber').value.trim(),
            chassisNumber: document.getElementById('chassisNumber').value.trim(),
            make: document.getElementById('make').value.trim(),
            model: document.getElementById('model').value.trim(),
            manufactureYear: yearVal ? parseInt(yearVal, 10) : null,
            vehicleType: document.getElementById('vehicleType').value,
            color: document.getElementById('color').value.trim(),
            engineNumber: document.getElementById('engineNumber').value.trim(),
            customerId: customerIdVal ? parseInt(customerIdVal, 10) : null,
            customerName: document.getElementById('customerName').value.trim(),
            customerContact: document.getElementById('customerContact').value.trim(),
            customerEmail: document.getElementById('customerEmail').value.trim(),
            customerAddress: document.getElementById('customerAddress').value.trim()
        };

        try {
            await api('/vehicles', { method: 'POST', body: JSON.stringify(payload) });
            showMessage(regMsg, 'Vehicle registered successfully!', 'success');
            regForm.reset();
            customerFields.forEach(f => { f.disabled = false; });
            document.getElementById('customerName').setAttribute('required', 'required');
            document.getElementById('customerContact').setAttribute('required', 'required');
            await refreshAll();
        } catch (err) {
            showMessage(regMsg, err.message, 'error');
        }
    });

    // Directory search & clear
    const searchInput = document.getElementById('searchKeyword');
    document.getElementById('searchBtn').addEventListener('click', () => {
        loadVehicles(searchInput.value.trim());
    });
    document.getElementById('clearSearchBtn').addEventListener('click', () => {
        searchInput.value = '';
        loadVehicles('');
    });
    searchInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            loadVehicles(searchInput.value.trim());
        }
    });

    // Spare part order form
    const orderForm = document.getElementById('orderForm');
    const orderMsg = document.getElementById('orderMessage');

    orderForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearMessage(orderMsg);

        const vehicleIdVal = document.getElementById('orderVehicleId').value;
        const unitPriceVal = document.getElementById('unitPrice').value;

        const payload = {
            vehicleId: vehicleIdVal ? parseInt(vehicleIdVal, 10) : null,
            partName: document.getElementById('partName').value.trim(),
            quantity: parseInt(document.getElementById('quantity').value, 10) || 1,
            unitPrice: unitPriceVal ? parseFloat(unitPriceVal) : 0
        };

        try {
            await api('/orders', { method: 'POST', body: JSON.stringify(payload) });
            showMessage(orderMsg, 'Spare part order placed successfully!', 'success');
            orderForm.reset();
            document.getElementById('quantity').value = '1';
            await refreshAll();
        } catch (err) {
            showMessage(orderMsg, err.message, 'error');
        }
    });

    // Initial data load
    refreshAll();
});
