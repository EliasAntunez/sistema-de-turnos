package com.example.sitema_de_turnos.excepcion;

/**
 * Excepción para errores en el sistema de notificaciones
 */
public class NotificationException extends RuntimeException {
    
    public NotificationException(String message) {
        super(message);
    }
    
    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Obtiene el mensaje de error raíz (causa más profunda)
     */
    public String getRootCauseMessage() {
        Throwable cause = this.getCause();
        while (cause != null && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause != null ? cause.getMessage() : this.getMessage();
    }
}
