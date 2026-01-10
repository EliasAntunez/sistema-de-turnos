package com.example.sitema_de_turnos.dto;

import com.example.sitema_de_turnos.modelo.EstadoTurno;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambiarEstadoTurnoRequest {
    
    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoTurno nuevoEstado;
    
    private String observaciones;
}
