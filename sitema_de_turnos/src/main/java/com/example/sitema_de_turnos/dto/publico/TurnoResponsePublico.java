package com.example.sitema_de_turnos.dto.publico;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para respuesta de turno en contexto PÚBLICO (cliente).
 * NO expone datos sensibles de otros clientes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoResponsePublico {

    private Long id;
    private Long servicioId;
    private String servicioNombre;
    private Long profesionalId;
    private String profesionalNombre;
    private String profesionalApellido;
    private String fecha;
    private String horaInicio;
    private String horaFin;
    private String horaFinServicio;
    private Integer duracionMinutos;
    private BigDecimal precio;
    private String estado;
    private String observaciones;
    private String empresaTelefono;
    private String empresaDatosBancarios;
}
