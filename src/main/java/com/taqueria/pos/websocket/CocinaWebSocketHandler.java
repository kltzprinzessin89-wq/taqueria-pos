package com.taqueria.pos.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CocinaWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sesiones = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sesiones.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sesiones.remove(session);
    }

    public void enviarATodos(String mensaje) {
        for (WebSocketSession sesion : sesiones) {
            if (!sesion.isOpen()) {
                sesiones.remove(sesion);
                continue;
            }
            try {
                synchronized (sesion) {
                    sesion.sendMessage(new TextMessage(mensaje));
                }
            } catch (IOException e) {
                sesiones.remove(sesion);
            }
        }
    }
}