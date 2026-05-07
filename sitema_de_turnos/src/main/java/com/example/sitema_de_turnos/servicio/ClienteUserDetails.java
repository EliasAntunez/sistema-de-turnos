package com.example.sitema_de_turnos.servicio;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * UserDetails personalizado para clientes.
 */
@RequiredArgsConstructor
@Getter
public class ClienteUserDetails implements UserDetails {

    private final Long clienteId;
    private final String empresaSlug;
    private final String email;
    private final String nombreUsuario;
    private final String password;
    private final boolean activo;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("CLIENTE"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        // Usar email como identificador principal, o nombreUsuario si no hay email
        return "cliente:" + empresaSlug + ":" + resolveIdentificador();
    }

    private String resolveIdentificador() {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return nombreUsuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return activo;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}