package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para configuración de recordatorios de una empresa.
 * 
 * Usado en endpoints de dueño para:
 * - Obtener configuración actual
 * - Actualizar configuración
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionRecordatoriosDTO {

    /**
     * Indica si los recordatorios automáticos están activados.
     */
    @NotNull(message = "El campo 'activarRecordatorios' es obligatorio")
    private Boolean activarRecordatorios;

    /**
     * Horas de anticipación para enviar recordatorios.
     * Debe ser mayor a 0.
     */
    @NotNull(message = "El campo 'horasAnticipacion' es obligatorio")
    @Min(value = 1, message = "Las horas de anticipación deben ser al menos 1")
    private Integer horasAnticipacion;
}
