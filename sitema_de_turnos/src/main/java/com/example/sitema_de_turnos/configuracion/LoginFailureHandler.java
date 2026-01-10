package com.example.sitema_de_turnos.configuracion;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handler personalizado para manejar errores de autenticación.
 * Devuelve mensajes descriptivos en formato JSON.
 */
@Component
@Slf4j
public class LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                       HttpServletResponse response,
                                       AuthenticationException exception) throws IOException, ServletException {
        
        String mensaje;
        
        // Determinar mensaje según el tipo de excepción
        if (exception instanceof BadCredentialsException) {
            mensaje = "Email o contraseña incorrectos";
            log.warn("Intento de login fallido - Credenciales incorrectas: {}", request.getParameter("username"));
            
        } else if (exception instanceof UsernameNotFoundException) {
            mensaje = "Usuario no encontrado";
            log.warn("Intento de login fallido - Usuario no existe: {}", request.getParameter("username"));
            
        } else if (exception instanceof DisabledException) {
            mensaje = "La cuenta está desactivada. Contacte al administrador";
            log.warn("Intento de login fallido - Usuario desactivado: {}", request.getParameter("username"));
            
        } else if (exception instanceof LockedException) {
            mensaje = "La cuenta está bloqueada. Contacte al administrador";
            log.warn("Intento de login fallido - Usuario bloqueado: {}", request.getParameter("username"));
            
        } else {
            mensaje = "Error de autenticación: " + exception.getMessage();
            log.error("Intento de login fallido - Error inesperado: {}", exception.getMessage(), exception);
        }
        
        // Construir respuesta JSON
        String jsonResponse = String.format(
            "{\"exito\":false,\"mensaje\":\"%s\",\"errores\":null}",
            escaparJson(mensaje)
        );
        
        // Enviar respuesta
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }
    
    /**
     * Escapa caracteres especiales para JSON
     */
    private String escaparJson(String texto) {
        if (texto == null) {
            return "";
        }
        return texto
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
