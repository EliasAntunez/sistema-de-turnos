package com.example.sitema_de_turnos.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BloqueoFechaResponse {

    private Long id;
    private Long profesionalId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String motivo;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
}
