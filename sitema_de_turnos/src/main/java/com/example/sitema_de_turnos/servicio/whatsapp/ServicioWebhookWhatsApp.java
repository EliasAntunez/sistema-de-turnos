package com.example.sitema_de_turnos.servicio.whatsapp;

import com.example.sitema_de_turnos.modelo.EstadoTurno;
import com.example.sitema_de_turnos.modelo.Turno;
import com.example.sitema_de_turnos.repositorio.RepositorioTurno;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Servicio para procesar webhooks de WhatsApp (respuestas de clientes).
 * 
 * Responsabilidades:
 * - Validar firmas HMAC de Meta para verificar autenticidad
 * - Verificar token de verificación inicial del webhook
 * - Procesar respuestas "SI" / "NO" de clientes
 * - Actualizar estado de turnos según respuestas
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ServicioWebhookWhatsApp {

    private final RepositorioTurno repositorioTurno;

    @Value("${whatsapp.webhook.verify-token}")
    private String verifyToken;

    @Value("${whatsapp.webhook.secret}")
    private String webhookSecret;

    /**
     * Valida el token de verificación del webhook (usado en setup inicial con Meta).
     * 
     * @param token Token recibido de Meta
     * @return true si el token coincide con el configurado
     */
    public boolean validarToken(String token) {
        boolean valido = verifyToken != null && verifyToken.equals(token);
        if (!valido) {
            log.warn("Token de verificación inválido recibido: {}", token);
        }
        return valido;
    }

    /**
     * Valida la firma HMAC SHA-256 del webhook.
     * Meta envía la firma en el header X-Hub-Signature-256.
     * 
     * @param payload Cuerpo del mensaje recibido
     * @param firmaRecibida Firma recibida en formato "sha256=XXXX"
     * @return true si la firma es válida
     */
    public boolean validarFirma(String payload, String firmaRecibida) {
        if (firmaRecibida == null || !firmaRecibida.startsWith("sha256=")) {
            log.warn("Firma inválida: formato incorrecto");
            return false;
        }

        try {
            // Calcular HMAC SHA-256
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                webhookSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            
            // Convertir a hex
            String firmaCalculada = "sha256=" + HexFormat.of().formatHex(hash);
            
            boolean valida = firmaCalculada.equals(firmaRecibida);
            
            if (!valida) {
                log.warn("Firma HMAC inválida. Esperada: {}, Recibida: {}", 
                    firmaCalculada.substring(0, 20) + "...", 
                    firmaRecibida.substring(0, 20) + "...");
            }
            
            return valida;
            
        } catch (Exception e) {
            log.error("Error al validar firma HMAC: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Procesa un mensaje recibido de WhatsApp (respuesta del cliente).
     * 
     * Busca el turno por messageId y actualiza su estado según la respuesta:
     * - "SI" → CONFIRMADO
     * - "NO" → CANCELADO
     * 
     * @param messageId ID del mensaje de WhatsApp (correlación con recordatorio enviado)
     * @param texto Texto del mensaje recibido
     * @param remitente Teléfono del remitente
     */
    public void procesarMensajeRecibido(String messageId, String texto, String remitente) {
        log.info("Procesando respuesta WhatsApp | MessageID: {} | Remitente: {} | Texto: '{}'", 
            messageId, remitente, texto);

        // 1. Buscar turno por whatsapp_message_id
        Optional<Turno> turnoOpt = repositorioTurno.findByWhatsappMessageId(messageId);
        
        if (turnoOpt.isEmpty()) {
            log.warn("No se encontró turno con messageId: {}. Posible respuesta a mensaje no relacionado con recordatorio.", 
                messageId);
            return;
        }
        
        Turno turno = turnoOpt.get();
        
        // 2. Verificar que el turno esté en estado PENDIENTE_CONFIRMACION
        if (turno.getEstado() != EstadoTurno.PENDIENTE_CONFIRMACION) {
            log.info("Turno {} no está en PENDIENTE_CONFIRMACION (estado actual: {}). Respuesta ignorada.", 
                turno.getId(), turno.getEstado());
            return;
        }
        
        // 3. Parsear respuesta
        String textoNormalizado = texto.trim().toUpperCase();
        String respuesta = null;
        EstadoTurno nuevoEstado = null;
        
        if (textoNormalizado.contains("SI") || textoNormalizado.equals("S") || textoNormalizado.equals("SÍ")) {
            respuesta = "SI";
            nuevoEstado = EstadoTurno.CONFIRMADO;
        } else if (textoNormalizado.contains("NO") || textoNormalizado.equals("N")) {
            respuesta = "NO";
            nuevoEstado = EstadoTurno.CANCELADO;
        } else {
            log.info("Respuesta no reconocida del turno {}: '{}'. Se esperaba SI o NO.", 
                turno.getId(), texto);
            return;
        }
        
        // 4. Actualizar turno
        turno.setEstado(nuevoEstado);
        turno.setFechaRespuestaRecordatorio(LocalDateTime.now());
        
        // Si cancela, agregar información adicional
        if (nuevoEstado == EstadoTurno.CANCELADO) {
            turno.setFechaCancelacion(LocalDateTime.now());
            turno.setMotivoCancelacion("Cancelado por cliente vía WhatsApp");
            turno.setCanceladoPor("CLIENTE");
        }
        
        repositorioTurno.save(turno);
        
        log.info("✓ Turno {} actualizado: {} → {} (respuesta: '{}')", 
            turno.getId(), 
            EstadoTurno.PENDIENTE_CONFIRMACION, 
            nuevoEstado, 
            respuesta);
    }
}
