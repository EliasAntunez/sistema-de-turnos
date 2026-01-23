package com.example.sitema_de_turnos.servicio.whatsapp;

import com.example.sitema_de_turnos.modelo.Cliente;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.Turno;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Implementación mock del servicio de WhatsApp.
 * 
 * NO envía mensajes reales, solo loguea la información.
 * Ideal para desarrollo y testing sin consumir cuota de Meta API.
 * 
 * Activo en profile 'dev' (por defecto si no se especifica profile).
 */
@Service
@Profile({"dev", "default"})
@Primary
@Slf4j
public class ServicioWhatsAppMock implements ServicioWhatsApp {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public String enviarRecordatorioTurno(Cliente cliente, Turno turno, Empresa empresa) {
        // Generar un message ID fake
        String messageId = "mock_wamid." + UUID.randomUUID().toString();
        
        // Construir el mensaje que se enviaría
        String mensaje = construirMensajeRecordatorio(cliente, turno, empresa);
        
        // Loguear en lugar de enviar
        log.info("📱 MOCK WhatsApp → {} | Turno: {} | Fecha: {} | Hora: {}", 
            cliente.getTelefono(),
            turno.getId(),
            turno.getFecha().format(FORMATO_FECHA),
            turno.getHoraInicio().format(FORMATO_HORA)
        );
        
        log.debug("Mensaje que se enviaría:\n{}", mensaje);
        log.debug("Message ID generado (mock): {}", messageId);
        
        return messageId;
    }

    /**
     * Construye el mensaje de recordatorio que se enviaría por WhatsApp.
     */
    private String construirMensajeRecordatorio(Cliente cliente, Turno turno, Empresa empresa) {
        return String.format(
            "Hola %s! 👋\n\n" +
            "Te recordamos tu turno en *%s*:\n" +
            "📅 Fecha: %s\n" +
            "⏰ Hora: %s\n" +
            "👤 Profesional: %s\n" +
            "💼 Servicio: %s\n\n" +
            "¿Confirmás tu asistencia?\n" +
            "Respondé *SI* para confirmar o *NO* para cancelar.",
            cliente.getNombre(),
            empresa.getNombre(),
            turno.getFecha().format(FORMATO_FECHA),
            turno.getHoraInicio().format(FORMATO_HORA),
            turno.getProfesional().getNombre(),
            turno.getServicio().getNombre()
        );
    }
}
