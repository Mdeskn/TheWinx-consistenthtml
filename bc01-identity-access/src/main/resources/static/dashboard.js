let vehicles  = [];
let myBookings = [];
let selectedVehicleId = null;

document.addEventListener("DOMContentLoaded", async () => {
    await refreshData();
});

// ─────────────────────────── Data ───────────────────────────

async function refreshData() {
    try {
        const [vRes, bRes] = await Promise.all([
            fetch(`${API_BASE}/vehicles`),
            fetch(`${BOOKING_BASE}/my?username=${encodeURIComponent(currentUsername)}`)
        ]);
        if (vRes.ok) vehicles   = await vRes.json();
        if (bRes.ok) myBookings = await bRes.json();
        filterVehicles();
        updateStats();
        renderBookings();
    } catch (err) {
        console.error("Failed to refresh data:", err);
    }
}

// ─────────────────────────── Vehicles ───────────────────────────

function renderVehicles(list) {
    const grid = document.getElementById("vehicleGrid");
    const none = document.getElementById("noResults");
    if (!grid) return;
    grid.innerHTML = "";
    if (!list.length) { if (none) none.style.display = "block"; return; }
    if (none) none.style.display = "none";

    list.forEach(v => {
        const avail = !!v.available;
        const card  = document.createElement("div");
        card.className  = "vehicle-card";
        card.dataset.id = v.id;
        card.innerHTML  = `
            <div class="vehicle-card-img" style="cursor:pointer" onclick="showVehicleDetail(${v.id})">${v.icon || "🚗"}</div>
            <div class="vehicle-card-body">
                <h4 style="cursor:pointer" onclick="showVehicleDetail(${v.id})">${v.name}</h4>
                <div class="vehicle-card-meta">${v.type} · ${v.seats} seat${v.seats !== 1 ? "s" : ""}</div>
                <div class="vehicle-card-footer">
                    <span class="price-tag">EUR ${v.pricePerDay}/${v.billingModel === "PER_HOUR" ? "hr" : "km"}</span>
                    <span class="availability-badge ${avail ? "avail-yes" : "avail-no"}">${avail ? "Available" : "Booked"}</span>
                </div>
                <div style="display:flex;gap:.5rem;margin-top:.5rem">
                    <button class="secondary-button" style="flex:1;font-size:.82rem" onclick="showVehicleDetail(${v.id})">Details</button>
                    <button class="btn-book" style="flex:1" ${avail ? "" : "disabled"} onclick="openBookModal(${v.id})">
                        ${avail ? "Book Now" : "Unavailable"}
                    </button>
                </div>
            </div>`;
        grid.appendChild(card);
    });
}

function filterVehicles() {
    const q     = (document.getElementById("searchInput")?.value || "").toLowerCase();
    const type  = document.getElementById("typeFilter")?.value  || "";
    const avail = document.getElementById("availFilter")?.value || "";
    const list  = vehicles.filter(v => {
        const mQ = !q    || (v.name || "").toLowerCase().includes(q);
        const mT = !type || (v.type || "") === type;
        const mA = avail === "" || String(v.available) === avail;
        return mQ && mT && mA;
    });
    renderVehicles(list);
}

// ─────────────────────────── Vehicle Detail ───────────────────────────

async function showVehicleDetail(vehicleId) {
    selectedVehicleId = vehicleId;
    const modal   = document.getElementById("vehicleDetailModal");
    const bookBtn = document.getElementById("vd-book-btn");
    document.getElementById("vd-name").textContent = "Loading…";
    document.getElementById("vd-info").innerHTML   = "";
    document.getElementById("vd-avg").textContent  = "–";
    document.getElementById("vd-stars").textContent = "";
    document.getElementById("vd-ratings").innerHTML = "";
    modal.showModal();

    try {
        const res  = await fetch(`${DETAIL_BASE}/${vehicleId}/detail`);
        if (!res.ok) { document.getElementById("vd-name").textContent = "Vehicle not found"; return; }
        const data = await res.json();
        const v    = data.vehicle || {};
        const ratings  = data.ratings  || [];
        const avgScore = data.averageScore || 0;
        const avail    = (v.status || "").toUpperCase() === "AVAILABLE";

        document.getElementById("vd-name").textContent = `${iconFor(v.type)} ${v.description || "Vehicle #" + vehicleId}`;

        const rows = [
            ["Type",     v.type || "-"],
            ["Status",   v.status || "-"],
            ["Price",    v.pricePerUnit != null ? `EUR ${v.pricePerUnit} / ${v.billingModel === "PER_HOUR" ? "hr" : "km"}` : "-"],
            ["Capacity", v.maxPersons != null ? `${v.maxPersons} person(s)` : "-"],
            ["Max duration", v.maxDurationMinutes != null ? `${v.maxDurationMinutes} min` : "-"],
            ["Max distance", v.maxKilometers != null ? `${v.maxKilometers} km` : "-"],
            ["Min age",  v.minAge != null ? `${v.minAge} yrs` : "-"],
        ];
        document.getElementById("vd-info").innerHTML = rows.map(([k, val]) =>
            `<tr><td style="padding:.22rem .5rem;color:#6b7285">${k}</td><td style="padding:.22rem .5rem;font-weight:600">${val}</td></tr>`
        ).join("");

        document.getElementById("vd-avg").textContent   = avgScore.toFixed(1);
        document.getElementById("vd-stars").textContent = starsFor(avgScore);

        const ul = document.getElementById("vd-ratings");
        ul.innerHTML = ratings.length ? ratings.slice(0, 5).map(r => `
            <li style="border-bottom:1px solid #e8edf5;padding:.45rem 0">
                <div style="display:flex;justify-content:space-between">
                    <span style="color:#f59e0b">${starsFor(r.vehicleScore)}</span>
                    <small style="color:#6b7285">${(r.createdAt || "").substring(0, 10)}</small>
                </div>
                ${r.comment ? `<div style="font-size:.88rem;margin-top:.2rem">${r.comment}</div>` : ""}
            </li>`).join("") :
            `<li style="color:#6b7285;font-style:italic;padding:.4rem 0">No reviews yet.</li>`;

        if (bookBtn) {
            bookBtn.disabled = !avail;
            bookBtn.textContent = avail ? "Book Now" : "Unavailable";
        }
    } catch (err) {
        document.getElementById("vd-name").textContent = "Error loading vehicle";
        console.error(err);
    }
}

function bookFromDetail() {
    document.getElementById("vehicleDetailModal").close();
    openBookModal(selectedVehicleId);
}

// ─────────────────────────── Book Modal (3 steps) ───────────────────────────

function openBookModal(vehicleId) {
    selectedVehicleId = vehicleId;
    const v = vehicles.find(x => x.id === vehicleId) || {};

    // Populate step 1
    const billingLabel = v.billingModel === "PER_HOUR" ? "per hour" : "per km";
    document.getElementById("bs1-title").textContent = v.name || `Vehicle #${vehicleId}`;
    document.getElementById("bs1-info").innerHTML = `
        <div style="font-size:2rem;margin-bottom:.4rem">${v.icon || "🚗"}</div>
        <div><strong>Type:</strong> ${v.type || "-"}</div>
        <div><strong>Billing:</strong> EUR ${v.pricePerDay || "-"} ${billingLabel}</div>
        <div><strong>Capacity:</strong> ${v.seats} seat${v.seats !== 1 ? "s" : ""}</div>
        <div><strong>Status:</strong> <span style="color:#44c67a;font-weight:600">AVAILABLE</span></div>`;

    showStep(1);
    document.getElementById("bookModal").showModal();
}

function goPaymentStep() {
    const v = vehicles.find(x => x.id === selectedVehicleId) || {};
    const billingLabel = v.billingModel === "PER_HOUR" ? "hr" : "km";
    document.getElementById("bs2-price").textContent =
        `Estimated rate: EUR ${v.pricePerDay || "-"}/${billingLabel}. Your card will be charged automatically when you end the ride.`;
    showStep(2);
}

function goVehicleStep() { showStep(1); }

function showStep(n) {
    document.getElementById("bookStep1").style.display = n === 1 ? "" : "none";
    document.getElementById("bookStep2").style.display = n === 2 ? "" : "none";
    document.getElementById("bookStep3").style.display = n === 3 ? "" : "none";

    const steps = [
        document.getElementById("mstep1"),
        document.getElementById("mstep2"),
        document.getElementById("mstep3"),
    ];
    steps.forEach((el, i) => {
        el.classList.remove("active", "done");
        if      (i + 1 < n) el.classList.add("done");
        else if (i + 1 === n) el.classList.add("active");
    });
    // update dots for done steps
    steps.forEach((el, i) => {
        const dot = el.querySelector(".step-dot");
        if (dot) dot.textContent = i + 1 < n ? "✓" : String(i + 1);
    });
}

function closeBookModal() {
    document.getElementById("bookModal").close();
    refreshData();
}

async function confirmBooking() {
    const btn = document.getElementById("bs2-confirm-btn");
    btn.disabled = true;
    btn.textContent = "Booking…";

    const payMethod = document.querySelector("input[name='payMethod']:checked")?.value || "CARD";

    try {
        const res = await fetch(`${BOOKING_BASE}/create`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ vehicleId: selectedVehicleId, username: currentUsername, paymentMethod: payMethod })
        });

        if (!res.ok) {
            const err = await res.text();
            alert("Booking failed: " + err);
            btn.disabled = false;
            btn.textContent = "Confirm Booking";
            return;
        }

        const booking = await res.json();

        // Populate step 3
        document.getElementById("cb-id").textContent      = booking.id || "-";
        document.getElementById("cb-vehicle").textContent = booking.vehicleName || `Vehicle #${selectedVehicleId}`;
        document.getElementById("cb-pay").textContent     = `${payMethod} - charged on ride end`;
        document.getElementById("cb-time").textContent    = booking.pickupDate || new Date().toLocaleString();

        showStep(3);
        await refreshData();
    } catch (err) {
        alert("Booking failed: " + err.message);
    } finally {
        btn.disabled = false;
        btn.textContent = "Confirm Booking";
    }
}

// ─────────────────────────── Cancel / End Ride ───────────────────────────

async function cancelBooking(bookingId) {
    if (!confirm("Cancel this booking?")) return;
    const res = await fetch(`${BOOKING_BASE}/${bookingId}/cancel`, { method: "POST" });
    if (!res.ok) { alert("Unable to cancel booking."); return; }
    await refreshData();
    showToast("Booking cancelled.", false);
}

async function endRide(bookingId) {
    if (!confirm("End this ride? Payment will be processed automatically.")) return;

    const res = await fetch(`${BOOKING_BASE}/${bookingId}/end`, { method: "POST" });
    if (!res.ok) { alert("Unable to end ride right now."); return; }

    const booking = await res.json();
    await refreshData();

    // Show payment result modal
    const totalStr = booking.totalCost != null ? `EUR ${booking.totalCost}` : "-";
    document.getElementById("pr-id").textContent      = booking.id || bookingId;
    document.getElementById("pr-vehicle").textContent = booking.vehicleName || "-";
    document.getElementById("pr-total").textContent   = totalStr;

    const rateUrl = `${RATING_URL}?bookingId=${bookingId}&userId=${currentUserId}` +
        `&vehicleId=${booking.vehicleId || ""}&providerId=${booking.providerId || ""}`;
    document.getElementById("pr-rate-link").href = rateUrl;

    document.getElementById("payResultModal").showModal();
}

// ─────────────────────────── Bookings Table ───────────────────────────

function renderBookings() {
    const tbody = document.getElementById("bookingsTbody");
    const noMsg = document.getElementById("noBookings");
    if (!tbody) return;
    tbody.innerHTML = "";

    if (!myBookings.length) { if (noMsg) noMsg.style.display = "block"; return; }
    if (noMsg) noMsg.style.display = "none";

    myBookings.forEach(b => {
        const status    = (b.status || "").toUpperCase();
        const pillClass = { CONFIRMED: "pill-confirmed", PENDING: "pill-pending",
            CANCELLED: "pill-cancelled", COMPLETED: "pill-completed" }[status] || "pill-pending";
        const canCancel = status === "CONFIRMED" || status === "PENDING";
        const canEnd    = status === "CONFIRMED";
        const canRate   = status === "COMPLETED";
        const rateUrl   = canRate
            ? `${RATING_URL}?bookingId=${b.id}&userId=${currentUserId}&vehicleId=${b.vehicleId || ""}&providerId=${b.providerId || ""}`
            : null;
        const totalStr  = b.totalCost && Number(b.totalCost) > 0 ? `EUR ${b.totalCost}` : "-";

        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${b.id}</td>
            <td><strong>${b.vehicleName}</strong></td>
            <td>${b.pickupDate}</td>
            <td>${b.returnDate}</td>
            <td><span class="status-pill ${pillClass}">${status}</span></td>
            <td>${totalStr}</td>
            <td>${canCancel ? `<button class="danger-button" onclick="cancelBooking(${b.id})">Cancel</button>` : "–"}</td>
            <td>${canEnd    ? `<button onclick="endRide(${b.id})">End Ride</button>` : "–"}</td>
            <td>${canRate   ? `<a class="btn-rate" href="${rateUrl}" target="_blank" style="font-size:.8rem;padding:.3rem .7rem">Rate</a>` : "–"}</td>`;
        tbody.appendChild(row);
    });
}

// ─────────────────────────── Stats ───────────────────────────

function updateStats() {
    const avail     = vehicles.filter(v => v.available).length;
    const active    = myBookings.filter(b => ["CONFIRMED","PENDING"].includes((b.status||"").toUpperCase())).length;
    const completed = myBookings.filter(b => (b.status||"").toUpperCase() === "COMPLETED").length;

    const setEl = (id, val) => { const el = document.getElementById(id); if (el) el.textContent = val; };
    setEl("stat-available",  avail);
    setEl("stat-my-bookings", myBookings.length);
    setEl("stat-active",     active);
    setEl("stat-completed",  completed);
}

// ─────────────────────────── Helpers ───────────────────────────

function iconFor(type) {
    if (!type) return "🚗";
    switch (type.toUpperCase()) {
        case "E_SCOOTER": return "🛴";
        case "BICYCLE":   return "🚲";
        case "E_BIKE":    return "🏍️";
        case "E_CAR":     return "🚗";
        default:          return "🚗";
    }
}

function starsFor(score) {
    const n = Math.round(score || 0);
    return "★".repeat(Math.max(0, Math.min(5, n))) + "☆".repeat(Math.max(0, 5 - Math.min(5, n)));
}

function showToast(msg, success = true) {
    const el  = document.getElementById("bookToast");
    const txt = document.getElementById("toastMsg");
    if (!el || !txt) return;
    txt.textContent = msg;
    el.className = `toast ${success ? "toast-success" : "toast-secondary"} show`;
    setTimeout(() => { el.className = el.className.replace(" show", ""); }, 3500);
}
