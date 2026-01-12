package com.example.sitema_de_turnos.configuracion;

import com.example.sitema_de_turnos.modelo.Cliente;
import com.example.sitema_de_turnos.servicio.ServicioAutenticacionCliente;
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
 * Handler para login exitoso de clientes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoginClienteSuccessHandler implements AuthenticationSuccessHandler {

    private final ServicioAutenticacionCliente servicioAutenticacionCliente;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {

        // Asegurar sesión
        HttpSession session = request.getSession(false);
        if (session == null) {
            session = request.getSession(true);
            log.debug("Nueva sesión creada para cliente: {}", session.getId());
        }

        // Parsear username: "cliente:{empresaSlug}:{telefono}"
        String username = authentication.getName();
        String[] parts = username.split(":", 3);
        String empresaSlug = parts[1];
        String telefono = parts[2];

        // Obtener datos del cliente
        Cliente cliente = servicioAutenticacionCliente.obtenerClienteParaAutenticacion(empresaSlug, telefono);

        log.info("Login exitoso - Cliente: {} - Empresa: {}", telefono, empresaSlug);

        // Construir respuesta JSON
        String jsonResponse = String.format(
            "{\"exito\":true,\"mensaje\":\"Autenticación exitosa\",\"datos\":{\"id\":%d,\"nombre\":\"%s\",\"telefono\":\"%s\",\"email\":\"%s\",\"empresaId\":%d,\"rol\":\"CLIENTE\"}}",
            cliente.getId(),
            escaparJson(cliente.getNombre()),
            escaparJson(cliente.getTelefono()),
            escaparJson(cliente.getEmail()),
            cliente.getEmpresa().getId()
        );

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }

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
