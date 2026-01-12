package com.example.sitema_de_turnos.servicio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsService principal que delega a los servicios específicos
 * según el formato del username.
 * 
 * - Si username comienza con "cliente:" → delega a ServicioDetallesCliente
 * - Si no → delega a ServicioDetallesUsuario (email)
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class ServicioDetallesUnificado implements UserDetailsService {

    private final ServicioDetallesUsuario servicioDetallesUsuario;
    private final ServicioDetallesCliente servicioDetallesCliente;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Intentando autenticar usuario: {}", username);
        
        // Si el username comienza con "cliente:", es un cliente
        if (username.startsWith("cliente:")) {
            log.debug("Delegando a ServicioDetallesCliente");
            return servicioDetallesCliente.loadUserByUsername(username);
        }
        
        // Si no, es un usuario del sistema (email)
        log.debug("Delegando a ServicioDetallesUsuario");
        return servicioDetallesUsuario.loadUserByUsername(username);
    }
}
