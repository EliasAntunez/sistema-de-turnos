package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para actualizar la configuración de una empresa (usado por el Dueño)
 * Incluye configuraciones operativas y de recordatorios
 */
@Data
public class ActualizarConfiguracionEmpresaRequest {

    /**
     * Buffer por defecto entre turnos en minutos
     * Rango válido: 0-120 minutos (0 min a 2 horas)
     */
    @NotNull(message = "El buffer por defecto es obligatorio")
    @Min(value = 0, message = "El buffer debe ser mayor o igual a 0")
    @Max(value = 120, message = "El buffer no puede exceder 120 minutos")
    private Integer bufferPorDefecto;

    /**
     * Tiempo mínimo de anticipación en minutos para reservar un turno
     * Rango válido: 0-1440 minutos (0 min a 24 horas)
     */
    @NotNull(message = "El tiempo mínimo de anticipación es obligatorio")
    @Min(value = 0, message = "El tiempo de anticipación debe ser mayor o igual a 0")
    @Max(value = 1440, message = "El tiempo de anticipación no puede exceder 1440 minutos (24 horas)")
    private Integer tiempoMinimoAnticipacionMinutos;

    /**
     * Días máximos hacia adelante que los clientes pueden reservar turnos
     * Rango válido: 1-365 días (1 día a 1 año)
     */
    @NotNull(message = "Los días máximos de reserva son obligatorios")
    @Min(value = 1, message = "Se debe permitir al menos 1 día de reserva")
    @Max(value = 365, message = "No se puede permitir más de 365 días de reserva")
    private Integer diasMaximosReserva;

    /**
     * Horas de anticipación antes del turno para enviar recordatorio
     * Rango válido: 1-168 horas (1 hora a 7 días)
     */
    @NotNull(message = "Las horas antes del recordatorio son obligatorias")
    @Min(value = 1, message = "Se debe enviar al menos 1 hora antes")
    @Max(value = 168, message = "No se puede enviar más de 168 horas (7 días) antes")
    private Integer horasAntesRecordatorio;

    /**
     * Indica si la empresa tiene habilitado el envío de recordatorios
     */
    @NotNull(message = "Debe indicar si se envían recordatorios")
    private Boolean enviarRecordatorios;

    /**
     * Datos bancarios para cobro de señas (CBU/Alias/instrucciones)
     * Campo opcional para permitir empresas sin configuración de transferencias.
     */
    private String datosBancarios;
}
