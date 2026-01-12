package com.example.sitema_de_turnos.configuracion;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Entry point que devuelve JSON 401 en lugar de redirigir a una página de login HTML.
 */
@Component
public class Http401UnauthorizedEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String json = String.format(
            "{\"exito\":false,\"mensaje\":\"No autenticado. Debe iniciar sesión.\",\"datos\":null,\"timestamp\":\"%s\"}",
            java.time.LocalDateTime.now()
        );
        
        response.getWriter().write(json);
    }
}
