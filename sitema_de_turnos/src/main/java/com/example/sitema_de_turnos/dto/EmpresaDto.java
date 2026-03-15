package com.example.sitema_de_turnos.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private String direccion;
    private String ciudad;
    private String provincia;
    private String telefono;
    private String email;
}
