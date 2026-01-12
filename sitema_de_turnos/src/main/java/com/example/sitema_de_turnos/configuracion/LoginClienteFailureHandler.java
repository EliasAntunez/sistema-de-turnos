package com.example.sitema_de_turnos.configuracion;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handler para login fallido de clientes.
 */
@Component
@Slf4j
public class LoginClienteFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                       HttpServletResponse response,
                                       AuthenticationException exception) throws IOException, ServletException {

        log.warn("Intento de login de cliente fallido: {}", exception.getMessage());

        String mensaje = "Teléfono o contraseña incorrectos";
        
        if (exception.getMessage().contains("desactivado")) {
            mensaje = "Su cuenta ha sido desactivada";
        } else if (exception.getMessage().contains("sin cuenta")) {
            mensaje = "Debe registrarse primero";
        }

        String jsonResponse = String.format(
            "{\"exito\":false,\"mensaje\":\"%s\",\"datos\":null}",
            mensaje
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }
}
