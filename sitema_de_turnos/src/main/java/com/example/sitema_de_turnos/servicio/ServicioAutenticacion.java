package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.LoginRequest;
import com.example.sitema_de_turnos.dto.LoginResponse;
import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.repositorio.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicioAutenticacion {

    private final AuthenticationManager authenticationManager;
    private final RepositorioUsuario repositorioUsuario;

    /**
     * Autentica un usuario y retorna sus datos básicos
     */
    public LoginResponse autenticar(LoginRequest request) {
        // 1. Autenticar con Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getContrasena()
                )
        );

        // 2. Si llega aquí, la autenticación fue exitosa
        String email = authentication.getName();

        // 3. Obtener datos del usuario
        Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // 4. Crear respuesta
        return new LoginResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol()
        );
    }
}
