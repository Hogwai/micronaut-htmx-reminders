const protocol = globalThis.location.protocol === 'https:' ? 'wss:' : 'ws:';
const wsUrl = `${protocol}//${globalThis.location.host}/ws/alerts`;
let ws;

function connectWebSocket() {
    ws = new WebSocket(wsUrl);

    ws.onopen = () => {
        console.log('Connected WebSocket');
    };

    ws.onmessage = (event) => {
        try {
            const alert = JSON.parse(event.data);
            console.log('Received alert:', alert);
            showNotification(alert);
        } catch (error) {
            console.error('Error while parsing alert:', error);
        }
    };

    ws.onerror = (error) => {
        console.error('WebSocket error:', error);
    };

    ws.onclose = () => {
        console.log('WebSocket disconnected, reconnection in 5s...');
        setTimeout(connectWebSocket, 5000);
    };
}

if ('Notification' in globalThis) {
    if (Notification.permission === 'default') {
        Notification.requestPermission().then(permission => {
            console.log('Notifications permission:', permission);
        });
    }
} else {
    console.warn('Notifications are not supported by this browser');
}

function showNotification(alert) {
    if (!('Notification' in globalThis)) {
        console.warn('Notifications not supported');
        return;
    }

    if (Notification.permission === 'granted') {
        const notification = new Notification(alert.title, {
            body: alert.description || 'Triggered alert',
            icon: '/icon.png',
            badge: '/badge.png',
            requireInteraction: true,
            tag: 'alert-' + alert.id
        });

        notification.onclick = () => {
            window.focus();
            notification.close();
        };
    } else if (Notification.permission === 'denied') {
        console.error('Notification permission denied');
    } else {
        Notification.requestPermission().then(permission => {
            if (permission === 'granted') {
                showNotification(alert);
            }
        });
    }
}

connectWebSocket();