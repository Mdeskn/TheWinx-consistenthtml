const BASE_URL = "http://localhost:8090";

async function getAllVehicles() {
    const res = await fetch(`${BASE_URL}/vehicles`);
    return res.json();
}

async function createVehicle(vehicle) {
    const res = await fetch(`${BASE_URL}/vehicles`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(vehicle)
    });
    return res.json();
}

async function updateStatus(id, status) {
    const res = await fetch(`${BASE_URL}/vehicles/${id}/status`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status })
    });
    return res.json();
}

async function updateLocation(id, lat, lon) {
    const res = await fetch(`${BASE_URL}/vehicles/${id}/location`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ latitude: lat, longitude: lon })
    });
    return res.json();
}

async function searchAvailable(lat, lon, radius) {
    const res = await fetch(`${BASE_URL}/vehicles/search?lat=${lat}&lon=${lon}&radius=${radius}`);
    return res.json();
}
