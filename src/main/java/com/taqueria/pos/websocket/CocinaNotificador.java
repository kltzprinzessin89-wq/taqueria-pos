package com.taqueria.pos.websocket;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CocinaNotificador {

    private final CocinaWebSocketHandler handler;

    public CocinaNotificador(CocinaWebSocketHandler handler) {
        this.handler = handler;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alOcurrirEvento(OrdenEvento evento) {
        String json = "{\"evento\":\"" + evento.tipo() + "\",\"ordenId\":" + evento.ordenId() + "}";
        handler.enviarATodos(json);
    }
}