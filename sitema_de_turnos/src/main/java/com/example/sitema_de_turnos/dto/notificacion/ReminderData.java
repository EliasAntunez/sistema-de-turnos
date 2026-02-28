package com.example.sitema_de_turnos.dto.notificacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO para transportar datos del recordatorio
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderData {
    
    private Long turnoId;
    
    // Datos del cliente
    private String clienteEmail;
    private String clienteNombre;
    
    // Datos del turno
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private LocalDateTime fechaHoraCompleta; // Para calcular horas restantes
    
    // Datos del servicio
    private String servicioNombre;
    private String profesionalNombre;
    
    // Datos de la empresa
    private String empresaNombre;
    private String empresaTelefono;
    private String empresaDireccion;
    private String empresaCiudad;
    private String empresaProvincia;
}
