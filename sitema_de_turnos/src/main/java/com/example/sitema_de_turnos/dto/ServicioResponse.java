package com.example.sitema_de_turnos.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ServicioResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private Integer duracionMinutos;
    private Integer bufferMinutos;
    private BigDecimal precio;
    private Boolean requiereSena;
    private BigDecimal montoSena;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
}
