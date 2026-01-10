package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReprogramacionTurnoDTO {
    
    @NotNull(message = "El ID del turno es requerido")
    private Long turnoId;
    
    @NotNull(message = "La nueva fecha es requerida")
    private LocalDate nuevaFecha;
    
    @NotNull(message = "La nueva hora es requerida")
    private LocalTime nuevaHora;
}
