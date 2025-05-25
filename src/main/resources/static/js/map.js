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

async function buildRoute(fromHistory = false) {
    if (routePoints.length < 2) {
        alert("Выберите 2 точки на карте.");
        return;
    }

    const [start, end] = routePoints;

    const polygons = blockedPoints.map(center => {
        return circleToPolygon(center, 40);
    });

    const requestBody = {
        points: [
            [start[1], start[0]],
            [end[1], end[0]]
        ],
        polygons: polygons.length > 0 ? polygons.map(ring => [ring]) : []
    };

    console.log("JSON для отправки:", JSON.stringify(requestBody, null, 2));

    fetch("/route", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
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
    if (!fromHistory){
        await saveRouteToHistory();
    }
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



document.getElementById("sidebar-toggle").addEventListener("click", () => {
    document.getElementById("sidebar").classList.toggle("open");
    document.getElementById("sidebar-toggle").style.left =
        document.getElementById("sidebar").classList.contains("open") ? "343px" : "0";
});

document.getElementById("user-avatar").addEventListener("click", () => {
    document.getElementById("user-menu").classList.toggle("hidden");
});

let currentAuthMode = 'login';

function showLogin() {
    currentAuthMode = 'login';
    document.getElementById("auth-title").textContent = "Вход";
    document.getElementById("auth-submit-btn").textContent = "Войти";
    document.getElementById("auth-dialog").classList.remove("hidden");
    document.getElementById("user-options").classList.add("hidden");
    document.getElementById("auth-buttons").classList.remove("hidden");
}

function showRegister() {
    currentAuthMode = 'register';
    document.getElementById("auth-title").textContent = "Регистрация";
    document.getElementById("auth-submit-btn").textContent = "Зарегистрироваться";
    document.getElementById("auth-dialog").classList.remove("hidden");
    document.getElementById("user-options").classList.add("hidden");
    document.getElementById("auth-buttons").classList.remove("hidden");
}

async function submitAuth() {
    const emailInput = document.getElementById("auth-email");
    const passwordInput = document.getElementById("auth-password");

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

    const formData = new URLSearchParams();
    formData.append('email', emailInput.value);
    formData.append('password', passwordInput.value);

    try {
        const endpoint = currentAuthMode === 'login' ? '/api/history/login' : '/api/history/register';

        const response = await fetch(`http://localhost:8080${endpoint}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: formData
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || (currentAuthMode === 'login' ? "Ошибка входа" : "Ошибка регистрации"));
        }

        const user = await response.json();
        handleSuccessfulAuth(user.email);

    } catch (error) {
        showValidationError(emailInput, error.message);
    }
}

function handleSuccessfulAuth(email) {
    localStorage.setItem('userEmail', email);


    document.getElementById("user-email").textContent = email;
    document.getElementById("auth-dialog").classList.add("hidden");
    document.getElementById("auth-buttons").classList.add("hidden");
    document.getElementById("user-options").classList.remove("hidden");

    showHistory();
}


function logout() {
    localStorage.removeItem('userEmail');
    document.getElementById("user-options").classList.add("hidden");
    document.getElementById("auth-buttons").classList.remove("hidden");
    clearMap();
}


async function saveRouteToHistory() {
    const userEmail = localStorage.getItem('userEmail');
    if (!userEmail) {
        alert('Сначала войдите или зарегистрируйтесь');
        return;
    }

    if (routePoints.length < 2) return;

    const [start, end] = routePoints;
    const polygons = blockedPoints.map(center => {
        const polygon = circleToPolygon(center, 40);
        return JSON.stringify(polygon);
    });

    try {
        const response = await fetch('/api/history/save', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: userEmail,
                lat1: start[0],
                lon1: end[0],
                lat2: start[1],
                lon2: end[1],
                polygons: polygons
            })
        });

        if (!response.ok) throw new Error('Ошибка сохранения');
        console.log('Маршрут сохранён!');
    } catch (error) {
        console.error(error);
    }
}

async function showHistory() {
    const userEmail = localStorage.getItem('userEmail');
    if (!userEmail) {
        alert('Сначала войдите или зарегистрируйтесь');
        return;
    }

    try {
        const response = await fetch(`/api/history/routes?email=${encodeURIComponent(userEmail)}`);
        if (!response.ok) throw new Error(await response.text());

        const routes = await response.json();
        const historyList = document.getElementById('history-list');
        historyList.innerHTML = '';

        if (routes.length === 0) {
            historyList.innerHTML = '<p>Нет сохраненных маршрутов</p>';
            return;
        }

        routes.forEach(route => {
            const item = document.createElement('div');
            item.className = 'history-item';
            item.innerHTML = `
                <p>Маршрут #${route.id} (${new Date(route.timestamp).toLocaleString('ru-Ru')})</p>
                <p>От: ${route.lat1.toFixed(6)}, ${route.lng1.toFixed(6)}</p>
                <p>До: ${route.lat2.toFixed(6)}, ${route.lng2.toFixed(6)}</p>
                <button onclick="loadHistory(${route.id})">Загрузить</button>
            `;
            historyList.appendChild(item);
        });

        document.getElementById('history-dialog').classList.remove('hidden');
    } catch (error) {
        console.error('Ошибка:', error);
        alert(error.message);
    }
}

async function loadHistory(routeId) {
    try {
        const routeResponse = await fetch(`/api/history/routes?email=${encodeURIComponent(localStorage.getItem('userEmail'))}`);
        const routes = await routeResponse.json();
        const route = routes.find(r => r.id === routeId);
        if (!route) throw new Error('Маршрут не найден');

        const blockedResponse = await fetch(`/api/history/blocked/${routeId}`);
        const blockedAreas = await blockedResponse.json();

        clearMap();
        routePoints = [
            [route.lat1, route.lng1],
            [route.lat2, route.lng2]
        ];

        routePoints.forEach((point, i) => {
            L.marker(point).addTo(markerLayer)
                .bindPopup(i === 0 ? "Начало" : "Конец")
                .openPopup();
        });

        console.log(blockedAreas)
        console.log(blockedResponse)
        blockedAreas.forEach(area => {
            const polygon = JSON.parse(area.polygonCoordinatesJson);
            const latlngs = polygon.map(([lng, lat]) => [lat, lng]);

            const centerLngLat = polygon[0];
            blockedPoints.push([centerLngLat[1], centerLngLat[0]]);

            L.polygon(latlngs, {
                color: 'red',
                fillOpacity: 0.4
            }).addTo(blockedLayer);
        });


        await buildRoute(true);
        document.getElementById('history-dialog').classList.add('hidden');
    } catch (error) {
        console.error(error);
    }
}


const avatar = document.getElementById("user-avatar");
const menu = document.getElementById("user-menu");

let menuOpen = false;

avatar.addEventListener("click", () => {
    const wasOpen = menuOpen;
    menuOpen = !wasOpen;
    menu.classList.toggle("hidden", !menuOpen);

    if (menuOpen) {
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
