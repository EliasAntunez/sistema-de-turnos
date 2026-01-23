package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.servicio.whatsapp.ServicioWebhookWhatsApp;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para webhooks de WhatsApp (Meta Cloud API).
 * 
 * Endpoints:
 * - GET: Verificación inicial del webhook (setup con Meta)
 * - POST: Recepción de eventos (mensajes entrantes de clientes)
 * 
 * Este endpoint es PÚBLICO (sin autenticación) pero validado con:
 * - Verify token (GET)
 * - Firma HMAC SHA-256 (POST)
 */
@RestController
@RequestMapping("/api/webhook/whatsapp")
@Slf4j
@RequiredArgsConstructor
public class ControladorWebhookWhatsApp {

    private final ServicioWebhookWhatsApp servicioWebhookWhatsApp;
    private final ObjectMapper objectMapper;

    /**
     * Endpoint de verificación del webhook (GET).
     * 
     * Meta llama a este endpoint durante el setup inicial del webhook para verificar
     * que el servidor es válido y responde correctamente.
     * 
     * Query params recibidos:
     * - hub.mode: "subscribe"
     * - hub.verify_token: Token configurado en Meta
     * - hub.challenge: String random que debemos devolver
     * 
     * Documentación: https://developers.facebook.com/docs/graph-api/webhooks/getting-started
     * 
     * @return El valor de hub.challenge si el token es válido, 403 en caso contrario
     */
    @GetMapping
    public ResponseEntity<String> verificarWebhook(
        @RequestParam(value = "hub.mode", required = false) String mode,
        @RequestParam(value = "hub.verify_token", required = false) String token,
        @RequestParam(value = "hub.challenge", required = false) String challenge
    ) {
        log.info("Solicitud de verificación de webhook | Mode: {} | Token: {}", mode, token != null ? "***" : "null");
        
        if ("subscribe".equals(mode) && servicioWebhookWhatsApp.validarToken(token)) {
            log.info("✓ Webhook verificado exitosamente");
            return ResponseEntity.ok(challenge);
        }
        
        log.warn("✘ Verificación de webhook fallida");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verificación fallida");
    }

    /**
     * Endpoint para recibir eventos de WhatsApp (POST).
     * 
     * Meta envía notificaciones cuando:
     * - Un cliente responde a un mensaje
     * - Cambia el estado de entrega de un mensaje
     * - Etc.
     * 
     * Estructura del payload (simplificada):
     * {
     *   "object": "whatsapp_business_account",
     *   "entry": [{
     *     "changes": [{
     *       "value": {
     *         "messages": [{
     *           "id": "wamid.XXX",
     *           "from": "5493764123456",
     *           "text": { "body": "SI" }
     *         }]
     *       }
     *     }]
     *   }]
     * }
     * 
     * @param firma Firma HMAC enviada por Meta en header X-Hub-Signature-256
     * @param payload Cuerpo del mensaje (JSON)
     * @return 200 OK si se procesa correctamente, 403 si la firma es inválida
     */
    @PostMapping
    public ResponseEntity<Void> recibirEvento(
        @RequestHeader(value = "X-Hub-Signature-256", required = false) String firma,
        @RequestBody String payload
    ) {
        log.debug("Evento recibido de WhatsApp | Tamaño payload: {} bytes", payload.length());
        
        // 1. Validar firma HMAC
        if (firma == null || !servicioWebhookWhatsApp.validarFirma(payload, firma)) {
            log.warn("✘ Firma HMAC inválida en webhook");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            // 2. Parsear JSON
            JsonNode root = objectMapper.readTree(payload);
            
            // 3. Verificar que es un evento de WhatsApp Business
            String object = root.path("object").asText();
            if (!"whatsapp_business_account".equals(object)) {
                log.debug("Evento ignorado: object={}", object);
                return ResponseEntity.ok().build();
            }
            
            // 4. Procesar cada entrada (normalmente hay una sola)
            JsonNode entries = root.path("entry");
            if (!entries.isArray()) {
                log.warn("Formato inesperado: 'entry' no es un array");
                return ResponseEntity.ok().build();
            }
            
            for (JsonNode entry : entries) {
                procesarEntry(entry);
            }
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error al procesar evento de WhatsApp: {}", e.getMessage(), e);
            // Devolver 200 para que Meta no reintente (error en nuestro lado, no de Meta)
            return ResponseEntity.ok().build();
        }
    }

    /**
     * Procesa una entrada individual del payload.
     */
    private void procesarEntry(JsonNode entry) {
        JsonNode changes = entry.path("changes");
        if (!changes.isArray()) {
            return;
        }
        
        for (JsonNode change : changes) {
            JsonNode value = change.path("value");
            
            // Verificar si hay mensajes entrantes
            JsonNode messages = value.path("messages");
            if (messages.isArray() && messages.size() > 0) {
                procesarMensajes(messages);
            }
            
            // Nota: También llegan otros eventos (statuses, etc.) que ignoramos por ahora
        }
    }

    /**
     * Procesa mensajes entrantes de clientes.
     */
    private void procesarMensajes(JsonNode messages) {
        for (JsonNode message : messages) {
            try {
                // Extraer datos del mensaje
                String messageId = message.path("id").asText();
                String remitente = message.path("from").asText();
                String texto = message.path("text").path("body").asText();
                
                // Validar que tenemos los datos necesarios
                if (messageId.isEmpty() || texto.isEmpty()) {
                    log.debug("Mensaje sin ID o texto, ignorado");
                    continue;
                }
                
                // Procesar respuesta del cliente
                servicioWebhookWhatsApp.procesarMensajeRecibido(messageId, texto, remitente);
                
            } catch (Exception e) {
                log.error("Error al procesar mensaje individual: {}", e.getMessage(), e);
            }
        }
    }
}
