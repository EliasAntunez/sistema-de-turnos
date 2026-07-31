package com.example.sitema_de_turnos.servicio.notificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class N8nWebhookNotificationService implements WhatsAppNotificationService {

    private static final Logger log = LoggerFactory.getLogger(N8nWebhookNotificationService.class);
    private final RestTemplate restTemplate;

    @Value("${app.integration.n8n.winback-webhook-url:}")
    private String n8nWebhookUrl;

    @Value("${app.integration.n8n.api-key:}")
    private String apiKeyHeader;

    public N8nWebhookNotificationService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public void enviarMensajeConInstancia(String telefono, String mensaje, String instanciaWhatsapp) {
        if (n8nWebhookUrl == null || n8nWebhookUrl.isBlank()) {
            log.warn("⚠️ URL del webhook de n8n no configurada. Mensaje Win-Back no enviado.");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKeyHeader != null && !apiKeyHeader.isBlank()) {
                headers.set("X-API-KEY", apiKeyHeader);
            }

            // Payload JSON estructurado
            Map<String, String> payload = new HashMap<>();
            payload.put("telefono", telefono);
            payload.put("mensaje", mensaje);
            payload.put("instanciaWhatsapp", instanciaWhatsapp);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(n8nWebhookUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("✅ Webhook Win-Back enviado a n8n para el número: {}", telefono);
            } else {
                log.error("❌ Falla al invocar webhook de n8n. Código de respuesta: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ Error al despachar webhook Win-Back a n8n: {}", e.getMessage());
        }
    }
}
