package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.RespuestaApi;
import com.example.sitema_de_turnos.dto.ApiResponse;
import com.example.sitema_de_turnos.dto.ClienteAutenticadoResponse;
import com.example.sitema_de_turnos.dto.PerfilUsuarioResponse;
import com.example.sitema_de_turnos.modelo.Cliente;
import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.repositorio.RepositorioUsuario;
import com.example.sitema_de_turnos.servicio.ServicioAutenticacionCliente;
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

    private final RepositorioUsuario repositorioUsuario;
    private final ServicioAutenticacionCliente servicioAutenticacionCliente;

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
     * Endpoint unificado para obtener el perfil del principal autenticado.
     * - Si es un cliente devuelve ApiResponse<ClienteAutenticadoResponse>
     * - Si es un usuario del sistema devuelve RespuestaApi<PerfilUsuarioResponse>
     * Si no hay autenticación, devuelve 200 con exito=false para que el frontend
     * pueda manejar el estado sin forzar redirect automático.
     */
    @GetMapping("/perfil")
    public ResponseEntity<?> perfil(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(RespuestaApi.error("No autenticado"));
        }

        String username = authentication.getName();

        // Cliente (username tiene formato cliente:{empresaSlug}:{telefono})
        if (username != null && username.startsWith("cliente:")) {
            String[] parts = username.split(":", 3);
            if (parts.length != 3) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Sesión de cliente inválida"));
            }
            String empresaSlug = parts[1];
            String telefono = parts[2];

            Cliente cliente = servicioAutenticacionCliente.obtenerClienteParaAutenticacion(empresaSlug, telefono);

            ClienteAutenticadoResponse perfil = new ClienteAutenticadoResponse(
                    cliente.getId(),
                    cliente.getNombre(),
                    cliente.getTelefono(),
                    cliente.getEmail(),
                    cliente.getEmpresa().getId(),
                    cliente.getEmpresa().getNombre()
            );

            return ResponseEntity.ok(ApiResponse.exito(perfil, "Perfil cliente obtenido"));
        }

        // Usuario del sistema (SUPER_ADMIN, DUENO, PROFESIONAL)
        Usuario usuario = repositorioUsuario.findByEmail(username).orElse(null);
        if (usuario == null) {
            return ResponseEntity.ok(RespuestaApi.error("Usuario no encontrado"));
        }

        PerfilUsuarioResponse perfilUsuario = new PerfilUsuarioResponse();
        perfilUsuario.setId(usuario.getId());
        perfilUsuario.setNombre(usuario.getNombre());
        perfilUsuario.setApellido(usuario.getApellido());
        perfilUsuario.setEmail(usuario.getEmail());
        perfilUsuario.setTelefono(usuario.getTelefono());
        perfilUsuario.setRol(usuario.getRol().name());
        perfilUsuario.setActivo(usuario.getActivo());

        return ResponseEntity.ok(RespuestaApi.exitosa("Perfil obtenido", perfilUsuario));
    }

    /**
     * NOTA: Endpoint de registro de SuperAdmin desactivado por seguridad.
     * Los SuperAdmins se crean automáticamente al iniciar la aplicación (ver InicializadorDatos.java)
     * o manualmente a través de scripts de base de datos.
     */

}
