package com.example.sitema_de_turnos.modelo;

public enum EstadoTurno {
    /**
     * Turno creado, esperando confirmación inicial
     */
    CREADO,
    
    /**
     * Turno pendiente de confirmación por el profesional o empresa
     */
    PENDIENTE_CONFIRMACION,
    
    /**
     * Turno confirmado por el profesional/empresa
     */
    CONFIRMADO,
    
    /**
     * Turno cancelado por cliente, profesional o empresa
     */
    CANCELADO,
    
    /**
     * Cliente fue atendido, turno completado
     */
    ATENDIDO,
    
    /**
     * Cliente no se presentó al turno
     */
    NO_ASISTIO
}
