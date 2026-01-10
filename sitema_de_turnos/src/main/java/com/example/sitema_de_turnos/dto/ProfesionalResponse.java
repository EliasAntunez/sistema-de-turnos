package com.example.sitema_de_turnos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalResponse {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private List<String> especialidades;
    private String descripcion;
    private Long empresaId;
    private String empresaNombre;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
