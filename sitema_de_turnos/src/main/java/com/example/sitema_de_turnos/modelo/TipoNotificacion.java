package com.example.sitema_de_turnos.modelo;

/**
 * Tipos de notificaciones que puede recibir un profesional
 */
public enum TipoNotificacion {
    /**
     * Se ha creado un nuevo turno asignado al profesional
     */
    NUEVO_TURNO,
    
    /**
     * Un cliente ha cancelado su turno
     */
    CANCELACION_CLIENTE,
    
    /**
     * Un cliente ha reprogramado su turno (cambio de fecha/hora)
     */
    REPROGRAMACION_CLIENTE,
    
    /**
     * Un cliente ha modificado datos del turno (observaciones, etc.)
     */
    MODIFICACION_TURNO,
    
    /**
     * La empresa/dueño ha cancelado un turno
     */
    CANCELACION_EMPRESA,
    
    /**
     * Recordatorio de turno próximo a iniciar
     */
    RECORDATORIO_TURNO,
    
    /**
     * Se ha creado un bloqueo que afecta turnos existentes
     */
    CONFLICTO_BLOQUEO,
    
    /**
     * Turno sin confirmar cerca de inicio
     */
    TURNO_SIN_CONFIRMAR
}
