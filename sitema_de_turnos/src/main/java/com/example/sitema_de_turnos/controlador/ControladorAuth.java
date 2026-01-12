package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.RespuestaApi;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class ControladorAuth {

    /**
     * Endpoint de health check para verificar que la API está funcionando.
     */
    @GetMapping("/health")
    public ResponseEntity<RespuestaApi<String>> health() {
        return ResponseEntity.ok(
                RespuestaApi.exitosa("API funcionando correctamente", "OK")
        );
    }

    /**
     * Endpoint para obtener CSRF token.
     * Spring Security 6 genera automáticamente el token al acceder al atributo del request.
     */
    @GetMapping("/csrf")
    public ResponseEntity<RespuestaApi<String>> getCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        
        if (csrfToken == null) {
            log.error("No se pudo obtener el token CSRF del request");
            return ResponseEntity.internalServerError().body(
                RespuestaApi.error("No se pudo generar el token CSRF")
            );
        }
        
        log.debug("Token CSRF generado para sesión");
        
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Token CSRF obtenido", csrfToken.getToken())
        );
    }

    /**
     * NOTA: Endpoint de registro de SuperAdmin desactivado por seguridad.
     * Los SuperAdmins se crean automáticamente al iniciar la aplicación (ver InicializadorDatos.java)
     * o manualmente a través de scripts de base de datos.
     */

}
