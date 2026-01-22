package com.example.sitema_de_turnos.configuracion;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro para logging de CSRF tokens.
 * Útil para debugging de problemas con validación de tokens CSRF.
 * 
 * En producción, configurar nivel de log a WARN o desactivar:
 * logging.level.com.example.sitema_de_turnos.configuracion.CsrfLoggingFilter=WARN
 */
@Component
@Slf4j
public class CsrfLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        // Solo logear en nivel DEBUG para POST/PUT/PATCH/DELETE
        String method = request.getMethod();
        if (log.isDebugEnabled() && 
            ("POST".equals(method) || "PUT".equals(method) || 
             "PATCH".equals(method) || "DELETE".equals(method))) {
            
            String uri = request.getRequestURI();
            log.debug("CSRF Validation - {} {}", method, uri);
            
            // Indicar presencia/ausencia del token sin loggear su valor literal
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            log.debug("CSRF token en sesión: {}", csrfToken != null ? "PRESENTE" : "NO_ENCONTRADO");

            // Token enviado por el cliente (no loggeamos el valor por seguridad)
            String headerToken = request.getHeader("X-XSRF-TOKEN");
            log.debug("CSRF token en header: {}", headerToken != null ? "PRESENTE" : "NO_ENVIADO");
            
            // Cookies
            if (request.getCookies() != null) {
                for (var cookie : request.getCookies()) {
                    if ("XSRF-TOKEN".equals(cookie.getName())) {
                        log.debug("Cookie XSRF-TOKEN: {}", cookie.getValue());
                    }
                    if ("JSESSIONID".equals(cookie.getName())) {
                        log.debug("Cookie JSESSIONID presente");
                    }
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
