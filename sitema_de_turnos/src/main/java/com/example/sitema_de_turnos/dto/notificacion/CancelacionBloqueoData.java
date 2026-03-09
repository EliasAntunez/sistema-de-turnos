package com.example.sitema_de_turnos.dto.notificacion;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Datos necesarios para enviar la notificación de cancelación por bloqueo.
 * Construido a partir de entidades Turno antes de ejecutar el bulk UPDATE,
 * mientras las relaciones (cliente, servicio, empresa) están cargadas en contexto.
 */
@Data
@Builder
public class CancelacionBloqueoData {

    private String clienteEmail;
    private String clienteNombre;

    private LocalDate fecha;
    private LocalTime horaInicio;

    private String servicioNombre;
    private String profesionalNombre;

    private String empresaNombre;
    /** Slug de la empresa, usado para construir el link de reserva: /reservar/{slug} */
    private String empresaSlug;
}
