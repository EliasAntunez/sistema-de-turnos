package com.example.sitema_de_turnos.dto.publico;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioPublicoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private Integer duracionMinutos;
    private BigDecimal precio;
}
