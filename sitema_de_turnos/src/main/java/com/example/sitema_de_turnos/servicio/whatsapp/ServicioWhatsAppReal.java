package com.example.sitema_de_turnos.servicio.whatsapp;

import com.example.sitema_de_turnos.modelo.Cliente;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.Turno;
import com.example.sitema_de_turnos.util.NormalizadorTelefono;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementación real del servicio de WhatsApp usando Meta WhatsApp Cloud API.
 * 
 * Requiere configuración de credenciales en application.properties o variables de entorno:
 * - whatsapp.api.url: URL base de la API (https://graph.facebook.com/v18.0)
 * - whatsapp.api.token: Token de acceso de Meta
 * - whatsapp.api.phone-number-id: ID del número de teléfono verificado
 * 
 * Activo solo en profile 'prod'.
 * 
 * Documentación oficial: https://developers.facebook.com/docs/whatsapp/cloud-api
 */
@Service
@Profile("prod")
@Slf4j
public class ServicioWhatsAppReal implements ServicioWhatsApp {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${whatsapp.api.url}")
    private String apiUrl;

    @Value("${whatsapp.api.token}")
    private String apiToken;

    @Value("${whatsapp.api.phone-number-id}")
    private String phoneNumberId;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    public ServicioWhatsAppReal(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String enviarRecordatorioTurno(Cliente cliente, Turno turno, Empresa empresa) {
        try {
            // 1. Normalizar teléfono a formato E.164
            String telefonoE164 = NormalizadorTelefono.normalizarE164(cliente.getTelefono());
            
            log.info("Enviando recordatorio WhatsApp → {} | Turno: {}", telefonoE164, turno.getId());
            
            // 2. Construir mensaje
            String mensaje = construirMensajeRecordatorio(cliente, turno, empresa);
            
            // 3. Preparar request para Meta API
            String url = String.format("%s/%s/messages", apiUrl, phoneNumberId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiToken);
            
            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("to", telefonoE164);
            body.put("type", "text");
            
            Map<String, String> text = new HashMap<>();
            text.put("body", mensaje);
            body.put("text", text);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            
            // 4. Enviar a Meta API
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
            );
            
            // 5. Parsear respuesta y extraer message ID
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                String messageId = rootNode.path("messages").get(0).path("id").asText();
                
                log.info("✓ WhatsApp enviado exitosamente | MessageID: {} | Turno: {}", 
                    messageId, turno.getId());
                
                return messageId;
            } else {
                throw new RuntimeException("Respuesta inesperada de WhatsApp API: " + response.getStatusCode());
            }
            
        } catch (HttpClientErrorException e) {
            log.error("Error HTTP al enviar WhatsApp | Status: {} | Body: {} | Turno: {}", 
                e.getStatusCode(), e.getResponseBodyAsString(), turno.getId());
            throw new RuntimeException("Error al enviar WhatsApp: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error("Error inesperado al enviar WhatsApp | Turno: {} | Error: {}", 
                turno.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al enviar WhatsApp: " + e.getMessage(), e);
        }
    }

    /**
     * Construye el mensaje de recordatorio que se enviará por WhatsApp.
     * 
     * Formato optimizado para WhatsApp:
     * - Usa emojis para mejorar legibilidad
     * - Texto en negrita con * *
     * - Instrucciones claras para responder
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
