package com.example.sitema_de_turnos.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket para notificaciones en tiempo real
 * 
 * STOMP (Simple Text Oriented Messaging Protocol) sobre WebSocket
 * permite comunicación bidireccional en tiempo real entre servidor y clientes
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // ✅ A3: CORS configurable desde properties
    @Value("${app.cors.allowed-origins}")
    private String allowedOriginsConfig;

    /**
     * Configurar el message broker
     * - /topic: canales de suscripción (broadcast a múltiples clientes)
     * - /app: prefijo para mensajes enviados desde el cliente al servidor
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilitar un simple message broker en memoria
        // Los clientes se suscriben a /topic/notifications/{profesionalId}
        config.enableSimpleBroker("/topic");
        
        // Prefijo para mensajes de aplicación
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registrar endpoints STOMP
     * Los clientes se conectan a /ws para establecer la conexión WebSocket
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ✅ A3: Parsear orígenes desde configuración (separados por comas)
        String[] allowedOrigins = allowedOriginsConfig.split(",");
        
        registry.addEndpoint("/ws")
                // Permitir CORS desde el frontend (configurable)
                .setAllowedOriginPatterns(allowedOrigins)
                // Habilitar SockJS como fallback para navegadores que no soporten WebSocket
                .withSockJS();
    }
}
