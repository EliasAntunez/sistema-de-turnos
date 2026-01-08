package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.ActualizarPerfilRequest;
import com.example.sitema_de_turnos.dto.PerfilUsuarioResponse;
import com.example.sitema_de_turnos.dto.RespuestaApi;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.repositorio.RepositorioUsuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para que los profesionales gestionen su propio perfil
 */
@RestController
@RequestMapping("/api/profesional")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PROFESIONAL')")
public class ControladorProfesional {

    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtener el perfil del profesional autenticado
     */
    @GetMapping("/perfil")
    public ResponseEntity<RespuestaApi<PerfilUsuarioResponse>> obtenerPerfil(Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        PerfilUsuarioResponse perfil = mapearAPerfilResponse(usuario);
        
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Perfil obtenido exitosamente", perfil)
        );
    }

    /**
     * Actualizar el perfil del profesional autenticado
     */
    @PutMapping("/perfil")
    public ResponseEntity<RespuestaApi<PerfilUsuarioResponse>> actualizarPerfil(
            @Valid @RequestBody ActualizarPerfilRequest request,
            Authentication authentication) {
        
        String email = authentication.getName();
        Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // Actualizar solo los campos proporcionados
        if (request.getNombre() != null) {
            usuario.setNombre(request.getNombre());
        }
        if (request.getApellido() != null) {
            usuario.setApellido(request.getApellido());
        }
        if (request.getEmail() != null && !request.getEmail().equals(usuario.getEmail())) {
            // Validar que el nuevo email no exista
            if (repositorioUsuario.existsByEmail(request.getEmail())) {
                throw new RuntimeException("El email ya está en uso");
            }
            usuario.setEmail(request.getEmail());
        }
        if (request.getTelefono() != null) {
            usuario.setTelefono(request.getTelefono());
        }
        if (request.getContrasena() != null && !request.getContrasena().isEmpty()) {
            usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));
        }

        Usuario actualizado = repositorioUsuario.save(usuario);
        PerfilUsuarioResponse perfil = mapearAPerfilResponse(actualizado);

        return ResponseEntity.ok(
                RespuestaApi.exitosa("Perfil actualizado exitosamente", perfil)
        );
    }

    private PerfilUsuarioResponse mapearAPerfilResponse(Usuario usuario) {
        PerfilUsuarioResponse response = new PerfilUsuarioResponse();
        response.setId(usuario.getId());
        response.setNombre(usuario.getNombre());
        response.setApellido(usuario.getApellido());
        response.setEmail(usuario.getEmail());
        response.setTelefono(usuario.getTelefono());
        response.setRol(usuario.getRol().getDescripcion());
        response.setActivo(usuario.getActivo());
        return response;
    }
}
