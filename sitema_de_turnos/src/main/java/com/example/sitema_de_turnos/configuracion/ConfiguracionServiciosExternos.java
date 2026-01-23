package com.example.sitema_de_turnos.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de beans para servicios externos.
 * 
 * - RestTemplate: Cliente HTTP para llamadas a APIs externas (ej: WhatsApp Cloud API)
 */
@Configuration
public class ConfiguracionServiciosExternos {

    /**
     * Bean de RestTemplate para realizar llamadas HTTP.
     * 
     * Configurado con timeouts razonables:
     * - Connection timeout: 10 segundos
     * - Read timeout: 30 segundos
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10 segundos
        factory.setReadTimeout(30000); // 30 segundos
        return new RestTemplate(factory);
    }
}
