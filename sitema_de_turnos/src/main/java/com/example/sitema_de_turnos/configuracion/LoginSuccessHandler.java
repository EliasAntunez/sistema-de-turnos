package com.example.sitema_de_turnos.configuracion;

import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.servicio.ServicioDetallesUsuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handler personalizado para manejar login exitoso.
 * Devuelve datos del usuario en formato JSON.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ServicioDetallesUsuario servicioDetallesUsuario;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        
        // Asegurar que existe una sesión
        HttpSession session = request.getSession(false);
        if (session == null) {
            session = request.getSession(true);
            log.debug("Nueva sesión creada: {}", session.getId());
        } else {
            log.debug("Sesión existente reutilizada: {}", session.getId());
        }
        
        // Obtener datos del usuario autenticado
        String email = authentication.getName();
        log.info("Login exitoso: {}", email);
        
        Usuario usuario = servicioDetallesUsuario.obtenerUsuarioPorEmail(email);
        
        // Construir respuesta JSON
        String jsonResponse = String.format(
            "{\"exito\":true,\"mensaje\":\"Autenticación exitosa\",\"datos\":{\"id\":%d,\"nombre\":\"%s\",\"apellido\":\"%s\",\"email\":\"%s\",\"rol\":\"%s\"}}",
            usuario.getId(),
            escaparJson(usuario.getNombre()),
            escaparJson(usuario.getApellido()),
            escaparJson(usuario.getEmail()),
            usuario.getRol().name()
        );
        
        response.setStatus(HttpServletResponse.SC_OK);
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
