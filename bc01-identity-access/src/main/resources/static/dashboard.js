let vehicles = [];
let myBookings = [];
let selectedVehicleId = null;

document.addEventListener("DOMContentLoaded", async () => {
    const today = new Date().toISOString().split("T")[0];
    const pickupDateInput = document.getElementById("pickupDate");
    const returnDateInput = document.getElementById("returnDate");
    if (pickupDateInput) pickupDateInput.min = today;
    if (returnDateInput) returnDateInput.min = today;
    await refreshData();
});

async function refreshData() {
    try {
        const [vehicleRes, bookingRes] = await Promise.all([
            fetch(`${API_BASE}/vehicles`),
            fetch(`${API_BASE}/bookings?username=${encodeURIComponent(currentUsername)}`)
        ]);

        if (vehicleRes.ok) {
            vehicles = await vehicleRes.json();
        }
        if (bookingRes.ok) {
            myBookings = await bookingRes.json();
        }

        filterVehicles();
        updateStats();
        renderBookings();
    } catch (err) {
        console.error("Failed to refresh dashboard data:", err);
    }
}

// ── Render vehicle cards ────────────────────────────────────────
function renderVehicles(list) {
    const grid = document.getElementById("vehicleGrid");
    const noResults = document.getElementById("noResults");
    if (!grid) return;
    grid.innerHTML = "";
    if (list.length === 0) {
        if (noResults) noResults.style.display = "block";
        return;
    }
    if (noResults) noResults.style.display = "none";
    list.forEach(v => {
        const card = document.createElement("div");
        card.className = "vehicle-card";
        card.dataset.id = v.id;
        card.innerHTML = `
            <div class="vehicle-card-img">${v.icon}</div>
            <div class="vehicle-card-body">
                <h4>${v.name}</h4>
                <div class="vehicle-card-meta">${v.type} · ${v.seats} seats · ${v.plate}</div>
                <div class="vehicle-card-footer">
                    <span class="price-tag">EUR ${v.pricePerDay}/day</span>
                    <span class="availability-badge ${v.available ? "avail-yes" : "avail-no"}">
                        ${v.available ? "Available" : "Booked"}
                    </span>
                </div>
                <button class="btn-book" ${v.available ? "" : "disabled"}
                    onclick="openBookModal(${v.id})">
                    ${v.available ? "Book Now" : "Unavailable"}
                </button>
            </div>`;
        grid.appendChild(card);
    });
}

// ── Filter ─────────────────────────────────────────────────────
function filterVehicles() {
    const searchInput = document.getElementById("searchInput");
    const typeFilter = document.getElementById("typeFilter");
    const availFilter = document.getElementById("availFilter");
    if (!searchInput || !typeFilter || !availFilter) return;

    const q     = searchInput.value.toLowerCase();
    const type  = typeFilter.value;
    const avail = availFilter.value;
    const filtered = vehicles.filter(v => {
        const matchQ = !q || v.name.toLowerCase().includes(q) || v.plate.toLowerCase().includes(q);
        const matchT = !type  || v.type === type;
        const matchA = avail === "" || String(v.available) === avail;
        return matchQ && matchT && matchA;
    });
    renderVehicles(filtered);
}

// ── Booking modal ──────────────────────────────────────────────
function openBookModal(vehicleId) {
    selectedVehicleId = vehicleId;
    const v = vehicles.find(x => x.id === vehicleId);
    if (!v) return;
    const bookVehicleInfo = document.getElementById("bookVehicleInfo");
    const pickupDate = document.getElementById("pickupDate");
    const returnDate = document.getElementById("returnDate");
    const bookNotes = document.getElementById("bookNotes");

    if (bookVehicleInfo) bookVehicleInfo.textContent = `${v.name} (${v.plate}) — EUR ${v.pricePerDay}/day`;
    if (pickupDate) pickupDate.value = "";
    if (returnDate) returnDate.value = "";
    if (bookNotes) bookNotes.value = "";
    new bootstrap.Modal(document.getElementById("bookModal")).show();
}

async function confirmBooking() {
    const pickupInput = document.getElementById("pickupDate");
    const returnInput = document.getElementById("returnDate");
    if (!pickupInput || !returnInput) return;

    const pickup = pickupInput.value;
    const ret    = returnInput.value;
    if (!pickup || !ret) { alert("Please select both pick-up and return dates."); return; }
    if (ret <= pickup)   { alert("Return date must be after pick-up date."); return; }

    const response = await fetch(`${API_BASE}/bookings`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            vehicleId: selectedVehicleId,
            username: currentUsername,
            pickupDate: pickup,
            returnDate: ret
        })
    });

    if (!response.ok) {
        alert("Unable to complete booking. Please try another vehicle.");
        return;
    }

    const booking = await response.json();
    await refreshData();

    const v = vehicles.find(x => x.id === booking.vehicleId);

    bootstrap.Modal.getInstance(document.getElementById("bookModal")).hide();
    showToast(`${v ? v.name : "Vehicle"} booked from ${pickup} to ${ret}!`);
}

async function cancelBooking(bookingId) {
    if (!confirm("Cancel this booking?")) return;

    const response = await fetch(`${API_BASE}/bookings/${bookingId}/cancel`, { method: "POST" });
    if (!response.ok) {
        alert("Unable to cancel booking right now.");
        return;
    }

    await refreshData();
    showToast("Booking cancelled.", false);
}

// ── Render bookings table ───────────────────────────────────────
function renderBookings() {
    const tbody = document.getElementById("bookingsTbody");
    const noMsg = document.getElementById("noBookings");
    if (!tbody) return;
    tbody.innerHTML = "";
    if (myBookings.length === 0) {
        if (noMsg) noMsg.style.display = "block";
        return;
    }
    if (noMsg) noMsg.style.display = "none";
    myBookings.forEach((b) => {
        const status = (b.status || "").toUpperCase();
        const pillClass = { CONFIRMED: "pill-confirmed", PENDING: "pill-pending",
            CANCELLED: "pill-cancelled", COMPLETED: "pill-completed" }[status] || "pill-pending";
        const canCancel = status === "CONFIRMED" || status === "PENDING";
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${b.id}</td>
            <td><strong>${b.vehicleName}</strong><br><small class="text-muted">${b.plate}</small></td>
            <td>${b.pickupDate}</td>
            <td>${b.returnDate}</td>
            <td><span class="status-pill ${pillClass}">${status || "PENDING"}</span></td>
            <td>${canCancel
                ? `<button class="btn btn-outline-danger btn-sm" onclick="cancelBooking(${b.id})">Cancel</button>`
                : "–"}</td>`;
        tbody.appendChild(row);
    });
}

// ── Stats ───────────────────────────────────────────────────────
function updateStats() {
    const available  = vehicles.filter(v => v.available).length;
    const active     = myBookings.filter(b => ["CONFIRMED", "PENDING"].includes((b.status || "").toUpperCase())).length;
    const completed  = myBookings.filter(b => (b.status || "").toUpperCase() === "COMPLETED").length;

    const statAvailable = document.getElementById("stat-available");
    const statMyBookings = document.getElementById("stat-my-bookings");
    const statActive = document.getElementById("stat-active");
    const statCompleted = document.getElementById("stat-completed");

    if (statAvailable) statAvailable.textContent = available;
    if (statMyBookings) statMyBookings.textContent = myBookings.length;
    if (statActive) statActive.textContent = active;
    if (statCompleted) statCompleted.textContent = completed;
}

// ── Toast helper ───────────────────────────────────────────────
function showToast(msg, success = true) {
    const toastEl = document.getElementById("bookToast");
    const toastMsg = document.getElementById("toastMsg");
    if (!toastEl || !toastMsg) return;
    toastMsg.textContent = msg;
    toastEl.className = `toast align-items-center text-bg-${success ? "success" : "secondary"} border-0`;
    bootstrap.Toast.getOrCreateInstance(toastEl, { delay: 3500 }).show();
}
