package com.example.sitema_de_turnos.dto;

import lombok.Data;

/**
 * DTO para respuesta de configuración de empresa
 * Contiene todas las configuraciones operativas y de recordatorios
 */
@Data
public class ConfiguracionEmpresaResponse {
    
    /**
     * Buffer por defecto entre turnos en minutos
     */
    private Integer bufferPorDefecto;

    /**
     * Tiempo mínimo de anticipación en minutos para reservar un turno
     */
    private Integer tiempoMinimoAnticipacionMinutos;

    /**
     * Días máximos hacia adelante que los clientes pueden reservar turnos
     */
    private Integer diasMaximosReserva;

    /**
     * Zona horaria de la empresa
     */
    private String timezone;

    /**
     * Horas de anticipación antes del turno para enviar recordatorio
     */
    private Integer horasAntesRecordatorio;

    /**
     * Indica si la empresa tiene habilitado el envío de recordatorios
     */
    private Boolean enviarRecordatorios;
}
