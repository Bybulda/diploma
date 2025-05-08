const map = L.map('map').setView([55.7558, 37.6173], 13);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);

let routePoints = [];
let blockedPoints = [];
let markerLayer = L.layerGroup().addTo(map);
let blockedLayer = L.layerGroup().addTo(map);
let routeLayers = [];
let activeRouteIndex = -1;

map.on('click', e => {
    if (e.originalEvent.cancelBubble) return;

    if (routePoints.length < 2) {
        routePoints.push([e.latlng.lat, e.latlng.lng]);
        L.marker(e.latlng).addTo(markerLayer)
            .bindPopup(routePoints.length === 1 ? "Начало" : "Конец")
            .openPopup();
    } else {
        alert("Уже выбраны 2 точки. Очистите карту.");
    }
});

map.on('contextmenu', e => {
    const latlng = [e.latlng.lat, e.latlng.lng];
    blockedPoints.push(latlng);
    L.circle(e.latlng, {
        radius: 40,
        color: 'red',
        fillOpacity: 0.4
    }).addTo(blockedLayer).bindPopup("Блокировка");
});

function buildRoute() {
    if (routePoints.length < 2) {
        alert("Выберите 2 точки на карте.");
        return;
    }

    const [start, end] = routePoints;

    const polygons = blockedPoints.map(center => {
        return circleToPolygon(center, 40); // возвращает List<List<Double>> как кольцо
    });

    const requestBody = {
        points: [
            [start[1], start[0]], // [lng, lat]
            [end[1], end[0]]
        ],
        polygons: polygons.length > 0 ? polygons.map(ring => [ring]) : [] // List<List<List<Double>>>
    };

    console.log("JSON для отправки:", JSON.stringify(requestBody, null, 2));

    fetch("/route", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody)
    })
        .then(r => r.json())
        .then(data => {
            clearRoutes();
            const colors = ['blue', 'green', 'orange'];
            data.paths.forEach((path, index) => {
                const latlngs = path.points.coordinates.map(([lng, lat]) => [lat, lng]);
                const polyline = L.polyline(latlngs, {
                    color: colors[index % colors.length],
                    weight: 5,
                    opacity: 0.5
                }).addTo(map);

                polyline.on('click', (e) => {
                    e.originalEvent.cancelBubble = true;
                    highlightRoute(index);
                });
                routeLayers.push(polyline);

                const routeDiv = document.createElement("div");
                routeDiv.className = "route-entry";
                routeDiv.textContent = `Маршрут ${index + 1}: ${(path.distance / 1000).toFixed(2)} км, ${(path.time / 60000).toFixed(1)} мин`;
                routeDiv.addEventListener('click', () => highlightRoute(index));
                document.getElementById("routeInfo").appendChild(routeDiv);
            });

            document.getElementById("sidebar").classList.add("open");
            updateRouteHighlight(0);
        });
}




function highlightRoute(index) {
    updateRouteHighlight(index);
    const bounds = routeLayers[index].getBounds();
    map.fitBounds(bounds);
}

function updateRouteHighlight(index) {
    activeRouteIndex = index;
    routeLayers.forEach((layer, i) => {
        layer.setStyle({
            opacity: i === index ? 1.0 : 0.4,
            weight: i === index ? 7 : 5
        });
    });

    document.querySelectorAll(".route-entry").forEach((el, i) => {
        el.classList.toggle("active", i === index);
    });
}

function clearRoutes() {
    routeLayers.forEach(r => map.removeLayer(r));
    routeLayers = [];
    document.getElementById("routeInfo").innerHTML = '';
}

function clearMap() {
    clearRoutes();
    markerLayer.clearLayers();
    blockedLayer.clearLayers();
    routePoints = [];
    blockedPoints = [];
    activeRouteIndex = -1;
    document.getElementById("routeInfo").innerText = "Выберите начальную и конечную точку.";
}

function circleToPolygon(center, radius, sides = 12) {
    const [lat, lng] = center;
    const coords = [];
    const angleStep = 360 / sides;
    for (let i = 0; i <= sides; i++) {
        const angle = angleStep * i;
        const dx = radius * Math.cos(angle * Math.PI / 180) / 111320;
        const dy = radius * Math.sin(angle * Math.PI / 180) / (40075000 * Math.cos(lat * Math.PI / 180) / 360);
        coords.push([lng + dx, lat + dy]);
    }
    return coords;
}


// Toggle sidebar
document.getElementById("sidebar-toggle").addEventListener("click", () => {
    document.getElementById("sidebar").classList.toggle("open");
    document.getElementById("sidebar-toggle").style.left =
        document.getElementById("sidebar").classList.contains("open") ? "343px" : "0";
});

document.getElementById("user-avatar").addEventListener("click", () => {
    document.getElementById("user-menu").classList.toggle("hidden");
});

function showLogin() {
    document.getElementById("auth-title").innerText = "Вход";
    document.getElementById("auth-dialog").classList.remove("hidden");
    document.getElementById("user-options").classList.add("hidden");
    document.getElementById("auth-buttons").classList.remove("hidden");
}

function showRegister() {
    document.getElementById("auth-title").innerText = "Регистрация";
    document.getElementById("auth-dialog").classList.remove("hidden");
    document.getElementById("user-options").classList.add("hidden");
    document.getElementById("auth-buttons").classList.remove("hidden");
}

function showHistory() {
    document.getElementById("history-dialog").classList.remove("hidden");
    document.getElementById("user-menu").classList.add("hidden");
}

function submitAuth() {
    const emailInput = document.getElementById("auth-email");
    const passwordInput = document.getElementById("auth-password");

    // Удаляем старые ошибки
    clearValidationErrors();

    let hasError = false;

    if (!emailInput.value.trim()) {
        showValidationError(emailInput, "Введите email");
        hasError = true;
    }

    if (!passwordInput.value.trim()) {
        showValidationError(passwordInput, "Введите пароль");
        hasError = true;
    }

    if (hasError) return;

    // Если всё введено, продолжаем
    const email = emailInput.value;
    const password = passwordInput.value;

    // TODO: отправить на бэкенд
    document.getElementById("user-email").innerText = email;
    document.getElementById("auth-dialog").classList.add("hidden");
    document.getElementById("auth-buttons").classList.add("hidden");
    document.getElementById("user-options").classList.remove("hidden");
}


function logout() {
    document.getElementById("user-options").classList.add("hidden");
    document.getElementById("auth-buttons").classList.remove("hidden");
}

function loadHistory(index) {
    // TODO: загрузить координаты маршрута и блокировок по индексу
    alert("Загрузка маршрута из истории " + index);
    document.getElementById("history-dialog").classList.add("hidden");
}

const avatar = document.getElementById("user-avatar");
const menu = document.getElementById("user-menu");

let menuOpen = false;

avatar.addEventListener("click", () => {
    const wasOpen = menuOpen;
    menuOpen = !wasOpen;
    menu.classList.toggle("hidden", !menuOpen);

    if (menuOpen) {
        // закрыть диалоги, если меню только что открылось
        document.getElementById("auth-dialog").classList.add("hidden");
        document.getElementById("history-dialog").classList.add("hidden");
    }
});



function closeAuthDialog() {
    document.getElementById("auth-dialog").classList.add("hidden");
}

function closeHistoryDialog() {
    document.getElementById("history-dialog").classList.add("hidden");
}

function showValidationError(inputEl, message) {
    inputEl.classList.add("input-error");
    const errorEl = document.createElement("div");
    errorEl.className = "error-message";
    errorEl.textContent = message;
    inputEl.parentNode.insertBefore(errorEl, inputEl.nextSibling);
}

function clearValidationErrors() {
    document.querySelectorAll(".input-error").forEach(el => el.classList.remove("input-error"));
    document.querySelectorAll(".error-message").forEach(el => el.remove());
}
