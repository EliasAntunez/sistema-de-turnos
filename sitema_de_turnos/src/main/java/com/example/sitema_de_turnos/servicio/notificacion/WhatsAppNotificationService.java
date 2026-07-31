package com.example.sitema_de_turnos.servicio.notificacion;

public interface WhatsAppNotificationService {
    void enviarMensajeConInstancia(String telefono, String mensaje, String instanciaWhatsapp);
}
