package com.hogwai.micronaut.htmx.reminders.websocket;

import com.hogwai.micronaut.htmx.reminders.domain.Alert;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micronaut.serde.ObjectMapper;

@Singleton
@ServerWebSocket("/ws/alerts")
public class AlertWebSocket {

    private static final Logger LOG = LoggerFactory.getLogger(AlertWebSocket.class);

    private WebSocketSession session;
    private final ObjectMapper objectMapper;

    public AlertWebSocket(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @OnOpen
    public void onOpen(WebSocketSession session) {
        this.session = session;
        LOG.info("WebSocket connecté");
    }

    @OnClose
    public void onClose(WebSocketSession session) {
        this.session = null;
        LOG.info("WebSocket déconnecté");
    }

    @OnMessage
    public void onMessage(String message, WebSocketSession session) {
        LOG.info("Message reçu: {}", message);
    }

    public void sendAlert(Alert alert) {
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(alert);
                session.sendSync(json);
                LOG.info("Alerte envoyée via WebSocket: {}", alert.getTitle());
            } catch (Exception e) {
                LOG.error("Erreur lors de l'envoi de l'alerte", e);
            }
        } else {
            LOG.warn("Aucune session WebSocket active pour envoyer l'alerte");
        }
    }
}
