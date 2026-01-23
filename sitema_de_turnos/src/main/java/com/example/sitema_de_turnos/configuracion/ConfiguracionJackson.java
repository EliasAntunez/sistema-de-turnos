package com.example.sitema_de_turnos.configuracion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Jackson para serialización/deserialización JSON.
 * Proporciona un ObjectMapper para el webhook de WhatsApp.
 */
@Configuration
public class ConfiguracionJackson {

    /**
     * Bean de ObjectMapper para parseo de JSON.
     * Spring Boot ya configura automáticamente el soporte para Java 8 Time API.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
