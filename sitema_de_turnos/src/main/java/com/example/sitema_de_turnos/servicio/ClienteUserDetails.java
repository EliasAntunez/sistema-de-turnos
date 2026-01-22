package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.modelo.Cliente;
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

    private final Cliente cliente;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("CLIENTE"));
    }

    @Override
    public String getPassword() {
        return cliente.getContrasena();
    }

    @Override
    public String getUsername() {
        return "cliente:" + cliente.getEmpresa().getSlug() + ":" + cliente.getTelefono();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return cliente.getActivo();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return cliente.getActivo();
    }
}