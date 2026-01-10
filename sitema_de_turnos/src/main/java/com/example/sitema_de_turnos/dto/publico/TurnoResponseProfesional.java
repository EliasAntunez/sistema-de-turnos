package com.example.sitema_de_turnos.dto.publico;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para respuesta de turno en contexto PROFESIONAL.
 * Incluye datos completos del cliente para gestión del profesional.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoResponseProfesional {

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
    
    // Datos del cliente (solo visible para el profesional)
    private String clienteNombre;
    private String clienteTelefono;
    private String clienteEmail;
    
    private String observaciones;
}
