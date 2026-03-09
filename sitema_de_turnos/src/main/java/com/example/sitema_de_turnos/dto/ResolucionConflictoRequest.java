package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResolucionConflictoRequest {

    @NotNull(message = "El bloqueo es requerido")
    private BloqueoFechaRequest bloqueo;

    @NotNull(message = "La acción es requerida")
    private AccionResolucion accion;

    public enum AccionResolucion {
        /** Cancela todos los turnos activos dentro del rango del bloqueo. */
        CANCELAR_EN_RANGO
    }
}
