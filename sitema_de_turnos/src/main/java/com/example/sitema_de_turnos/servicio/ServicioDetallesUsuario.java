package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.repositorio.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Servicio auxiliar para cargar detalles de usuarios del sistema.
 * 
 * NO implementa UserDetailsService directamente para evitar conflictos de beans.
 */
@Component
@RequiredArgsConstructor
public class ServicioDetallesUsuario {

    private final RepositorioUsuario repositorioUsuario;

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + email);
        }

        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(usuario.getRol().name())
        );

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getContrasena())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!usuario.getActivo())
                .build();
    }

    public Usuario obtenerUsuarioPorEmail(String email) {
        return repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
