package com.example.sitema_de_turnos.servicio.whatsapp;

import com.example.sitema_de_turnos.modelo.Cliente;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.Turno;

/**
 * Interfaz para el servicio de envío de recordatorios por WhatsApp.
 * 
 * Permite abstraer la implementación real de Meta WhatsApp Cloud API
 * y facilitar testing con implementaciones mock.
 * 
 * Implementaciones:
 * - ServicioWhatsAppMock: Para desarrollo/testing (solo loguea)
 * - ServicioWhatsAppReal: Para producción (envío real a Meta API)
 */
public interface ServicioWhatsApp {

    /**
     * Envía un recordatorio de turno al cliente por WhatsApp.
     * 
     * @param cliente Cliente destinatario del recordatorio
     * @param turno Turno sobre el cual se envía el recordatorio
     * @param empresa Empresa a la que pertenece el turno
     * @return ID del mensaje de WhatsApp (wamid.XXX) para correlacionar respuestas
     * @throws RuntimeException Si falla el envío
     */
    String enviarRecordatorioTurno(Cliente cliente, Turno turno, Empresa empresa);
}
