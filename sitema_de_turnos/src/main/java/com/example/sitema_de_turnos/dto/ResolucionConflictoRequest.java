package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ResolucionConflictoRequest {
    
    @NotNull(message = "El bloqueo es requerido")
    private BloqueoFechaRequest bloqueo;
    
    @NotNull(message = "La acción es requerida")
    private AccionResolucion accion;
    
    private List<ReprogramacionTurnoDTO> reprogramaciones;
    
    public enum AccionResolucion {
        CANCELAR_TODOS,
        REPROGRAMAR,
        CANCELAR_FUTUROS
    }
}
