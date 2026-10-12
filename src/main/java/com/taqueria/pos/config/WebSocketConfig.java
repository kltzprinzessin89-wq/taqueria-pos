package com.taqueria.pos.config;

import com.taqueria.pos.websocket.CocinaWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CocinaWebSocketHandler cocinaHandler;

    public WebSocketConfig(CocinaWebSocketHandler cocinaHandler) {
        this.cocinaHandler = cocinaHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(cocinaHandler, "/ws/cocina")
                .setAllowedOriginPatterns("*");
    }
}