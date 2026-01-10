package com.example.sitema_de_turnos.dto.publico;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoResponse {

    private Long id;
    private Long servicioId;
    private String servicioNombre;
    private Long profesionalId;
    private String profesionalNombre;
    private String profesionalApellido;
    private String fecha;
    private String horaInicio;
    private String horaFin;
    private Integer duracionMinutos;
    private Integer bufferMinutos;
    private BigDecimal precio;
    private String estado;
    private String clienteNombre;
    private String clienteTelefono;
    private String clienteEmail;
    private String observaciones;
}
