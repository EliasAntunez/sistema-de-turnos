package com.example.sitema_de_turnos.configuracion;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class ConfiguracionSeguridad {

    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            // CSRF deshabilitado para API REST stateless
            // Justificación: Esta API usa HTTP Basic Auth sin sesiones (STATELESS).
            // CSRF protege contra ataques desde el navegador que explotan cookies de sesión,
            // pero en APIs stateless sin cookies de sesión, CSRF no es un vector de ataque relevante.
            // Si en el futuro se implementa autenticación basada en sesiones (cookies),
            // CSRF debe habilitarse con tokens apropiados.
            .csrf(csrf -> csrf.disable())
            .httpBasic(basic -> {})  // Habilitar HTTP Basic Auth
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/publico/**").permitAll()
                
                // Endpoints de SuperAdmin
                .requestMatchers("/api/admin/**").hasAuthority("SUPER_ADMIN")
                
                // Endpoints de Dueño
                .requestMatchers("/api/dueno/**").hasAuthority("DUENO")
                
                // Endpoints de Profesional
                .requestMatchers("/api/profesional/**").hasAuthority("PROFESIONAL")
                
                // Endpoints de Cliente
                .requestMatchers("/api/cliente/**").hasAuthority("CLIENTE")
                
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
