package com.example.sitema_de_turnos.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EspecialidadResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activa;
    private LocalDateTime fechaCreacion;
}
