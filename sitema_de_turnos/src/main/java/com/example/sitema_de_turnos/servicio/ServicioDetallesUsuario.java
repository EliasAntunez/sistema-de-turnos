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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio auxiliar para cargar detalles de usuarios del sistema.
 * Carga el SET COMPLETO de roles, habilitando el modelo multi-rol.
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

        // Cargar TODOS los roles del usuario como authorities
        List<GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(rol.name()))
                .collect(Collectors.toList());

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
