package com.example.sitema_de_turnos.dto;

import com.example.sitema_de_turnos.modelo.TipoNotificacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta de notificación
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionResponse {
    
    private Long id;
    private TipoNotificacion tipo;
    private String titulo;
    private String mensaje;
    private String contenidoJson;
    private Long turnoId;
    private Boolean leida;
    private String fechaCreacion;
    private String fechaLectura;
}
