package com.example.sitema_de_turnos.configuracion;

import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.repositorio.RepositorioUsuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handler de login exitoso que retorna JSON con datos del usuario.
 * Se ejecuta después de una autenticación exitosa con form login.
 */
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final RepositorioUsuario repositorioUsuario;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        String email = authentication.getName();
        Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Retornar JSON con datos del usuario
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        
        String jsonResponse = String.format(
            "{\"exito\":true,\"mensaje\":\"Login exitoso\",\"datos\":{" +
            "\"id\":%d," +
            "\"nombre\":\"%s\"," +
            "\"apellido\":\"%s\"," +
            "\"email\":\"%s\"," +
            "\"rol\":\"%s\"" +
            "}}",
            usuario.getId(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getEmail(),
            usuario.getRol().name()
        );
        
        response.getWriter().write(jsonResponse);
    }
}
