package com.example.sitema_de_turnos.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class ConfiguracionCors {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir peticiones desde el frontend de Vue.js
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",  // Vite dev server
                "http://localhost:3000",  // Vue CLI
                "http://localhost:8081",  // Alternativo
                "http://127.0.0.1:5173"
        ));
        
        // PRODUCCI\u00d3N: Usar UN SOLO origin
        // configuration.setAllowedOrigins(Arrays.asList("https://tudominio.com"));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // Headers espec\u00edficos (NO usar "*" en producci\u00f3n por seguridad)
        // X-XSRF-TOKEN requerido para CSRF protection
        configuration.setAllowedHeaders(Arrays.asList(
                "Content-Type",
                "Authorization",
                "X-Requested-With",
                "X-XSRF-TOKEN"
        ));
        
        // Exponer header XSRF-TOKEN para que frontend pueda leerlo
        configuration.setExposedHeaders(Arrays.asList("X-XSRF-TOKEN"));
        
        // Requerido para enviar cookies de sesi\u00f3n
        configuration.setAllowCredentials(true);
        
        // Cachear preflight requests por 1 hora
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
