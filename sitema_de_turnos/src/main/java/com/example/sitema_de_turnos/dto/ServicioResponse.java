package com.example.sitema_de_turnos.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ServicioResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private Integer duracionMinutos;
    private Integer bufferMinutos;
    private BigDecimal precio;
    private Set<String> especialidades; // Nombres de especialidades
    private Boolean activo;
    private LocalDateTime fechaCreacion;
}
