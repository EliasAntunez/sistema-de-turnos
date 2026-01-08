package com.example.sitema_de_turnos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private String cuit;
    private String direccion;
    private String ciudad;
    private String provincia;
    private String telefono;
    private String email;
    private Boolean activa;
    private DuenoResponse dueno;
    private LocalDateTime fechaCreacion;
}
