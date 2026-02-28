package com.example.sitema_de_turnos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para enviar notificación en tiempo real vía WebSocket
 * Versión ligera sin datos innecesarios
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionWebSocketDTO {
    
    private Long id;
    private String tipo;
    private String titulo;
    private String mensaje;
    private String contenidoJson;
    private Long turnoId;
    private String fechaCreacion;
}
