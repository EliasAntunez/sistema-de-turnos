package com.example.sitema_de_turnos.servicio.scheduler;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para el sistema de recordatorios
 */
@Configuration
@Getter
public class ReminderConfig {
    
    @Value("${app.reminder.enabled:true}")
    private boolean enabled;
    
    @Value("${app.reminder.hours-before:24}")
    private int hoursBefore;
    
    /**
     * ✅ A3: Minutos mínimos antes del turno para enviar recordatorio.
     * 
     * Define el límite inferior de la ventana de recordatorios.
     * Si un turno está más cerca que este valor, NO se envía recordatorio
     * (ya es muy tarde para que el aviso sea útil).
     * 
     * Ejemplo: Si es 30, no enviar recordatorio a turnos en menos de 30 minutos.
     */
    @Value("${app.reminder.min-minutes-before:30}")
    private int minMinutesBefore;
    
    @Value("${app.reminder.max-retries:3}")
    private int maxRetries;
    
    @Value("${app.reminder.retry-delay-ms:2000}")
    private long retryDelayMs;
}
