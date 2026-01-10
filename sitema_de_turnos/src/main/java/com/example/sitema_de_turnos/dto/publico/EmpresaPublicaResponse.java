package com.example.sitema_de_turnos.dto.publico;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaPublicaResponse {
    private Long id;
    private String nombre;
    private String slug;
    private String descripcion;
    private String direccion;
    private String ciudad;
    private String provincia;
    private String telefono;
    private String email;
    private Integer diasMaximosReserva;
}
