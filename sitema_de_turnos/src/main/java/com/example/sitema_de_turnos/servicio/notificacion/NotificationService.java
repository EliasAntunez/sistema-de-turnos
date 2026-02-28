package com.example.sitema_de_turnos.servicio.notificacion;

import com.example.sitema_de_turnos.dto.notificacion.ReminderData;

/**
 * Interfaz para servicios de notificación (Email, SMS, WhatsApp, etc.)
 * Permite implementar diferentes canales de comunicación
 */
public interface NotificationService {
    
    /**
     * Envía un recordatorio de turno
     * @param data Datos del turno y cliente
     * @throws com.example.sitema_de_turnos.excepcion.NotificationException si falla el envío
     */
    void sendReminder(ReminderData data);
    
    /**
     * Verifica si el servicio de notificación está disponible
     * @return true si está configurado y operativo
     */
    boolean isAvailable();
    
    /**
     * Retorna el tipo de notificación (EMAIL, SMS, WHATSAPP)
     */
    String getNotificationType();
}
