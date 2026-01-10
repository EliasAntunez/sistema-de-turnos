package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BloqueoFechaRequest {

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    /**
     * Fecha de fin del bloqueo (opcional).
     * Si es null, el bloqueo es solo para la fecha_inicio.
     */
    private LocalDate fechaFin;

    /**
     * Motivo del bloqueo (opcional)
     */
    private String motivo;
}
