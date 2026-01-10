package com.example.sitema_de_turnos.dto.publico;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalPublicoResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String descripcion;
}
