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
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Configuración de seguridad con sesiones stateful y cookies seguras.
 * 
 * DECISIÓN ARQUITECTÓNICA:
 * - Sesiones stateful (IF_REQUIRED) para validar estado del usuario en cada request
 * - CSRF habilitado (protección contra ataques cross-site con cookies)
 * - Cookies HttpOnly + Secure (no accesibles desde JavaScript)
 * - Invalidación de sesiones al cambiar contraseña o desactivar usuario
 * 
 * MIGRACIÓN DESDE HTTP BASIC + STATELESS:
 * - Antes: Credenciales en localStorage, sin validación de usuario existente
 * - Ahora: Sesiones en servidor, validación en cada request, cookies seguras
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class ConfiguracionSeguridad {

    private final CorsConfigurationSource corsConfigurationSource;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final CsrfLoggingFilter csrfLoggingFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * SessionRegistry para rastrear sesiones activas.
     * Necesario para invalidar sesiones manualmente (ej: al cambiar contraseña).
     */
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /**
     * HttpSessionEventPublisher para notificar eventos de sesión al SessionRegistry.
     * Necesario para que el SessionRegistry funcione correctamente.
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /**
     * CsrfTokenRepository para gestionar tokens CSRF.
     * Expuesto como bean para poder inyectarlo en otros componentes.
     */
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // Agregar filtro de logging ANTES del CsrfFilter
            .addFilterBefore(csrfLoggingFilter, CsrfFilter.class)
            
            // CSRF habilitado con cookie (requerido para sesiones stateful)
            // CookieCsrfTokenRepository: Token CSRF almacenado en cookie
            // Frontend debe leer cookie XSRF-TOKEN y enviarla como header X-XSRF-TOKEN
            // CsrfTokenRequestAttributeHandler: Hace el token disponible en el request
            .csrf(csrf -> {
                CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
                requestHandler.setCsrfRequestAttributeName("_csrf");
                
                csrf
                    .csrfTokenRepository(csrfTokenRepository())
                    .csrfTokenRequestHandler(requestHandler)
                    .ignoringRequestMatchers(
                        "/api/publico/**",        // Endpoints públicos
                        "/api/auth/login",        // Login usuarios no requiere CSRF
                        "/api/auth/logout",       // Logout manejado por Spring Security
                        "/api/auth/perfil",       // Perfil para verificar sesión
                        "/api/webhook/whatsapp"   // Webhook de WhatsApp (validado con HMAC)
                    );
            })
            
            // Autenticación por formulario (credenciales en body, no headers)
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .successHandler(loginSuccessHandler)
                .failureHandler(loginFailureHandler)
            )
            
            // Logout con invalidación de sesión
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(200);
                    response.setContentType("application/json");
                    response.getWriter().write(
                        "{\"exito\":true,\"mensaje\":\"Sesión cerrada exitosamente\",\"data\":null}"
                    );
                })
            )
            
            // Gestión de sesiones stateful
            .sessionManagement(session -> session
                // IF_REQUIRED: Crear sesión solo cuando sea necesario (ej: después del login)
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                
                // Cambiar ID de sesión después del login (previene session fixation)
                .sessionAuthenticationStrategy(new ChangeSessionIdAuthenticationStrategy())
                
                // Máximo 1 sesión activa por usuario
                // Si intenta login desde otro dispositivo, invalida sesión anterior
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false) // false = nueva sesión invalida anterior
                .expiredUrl("/api/auth/session-expired")
                .sessionRegistry(sessionRegistry()) // AGREGADO: Usar el SessionRegistry
            )

            // Manejo de errores para APIs: devolver JSON en vez de redirecciones HTML
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"exito\":false,\"mensaje\":\"No autenticado\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"exito\":false,\"mensaje\":\"Acceso denegado\"}");
                })
            )
            
            .authorizeHttpRequests(auth -> auth
                // Allow preflight CORS requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Endpoints públicos (sin autenticación)
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/publico/**").permitAll()
                .requestMatchers("/api/webhook/whatsapp").permitAll() // Webhook WhatsApp (validado con HMAC)
                // Hacer público el endpoint de políticas activas
                .requestMatchers("/api/politicas-cancelacion/empresa/*/activas").permitAll()
                
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
