package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.modelo.Cliente;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Servicio auxiliar para cargar detalles de clientes.
 * 
 * IMPORTANTE: Este servicio se usa SOLO cuando el username sigue el formato:
 * "cliente:{empresaSlug}:{telefono}"
 * 
 * Esto permite diferenciar entre:
 * - Usuarios del sistema (Usuario entity): email directo
 * - Clientes (Cliente entity): prefijo "cliente:"
 * 
 * NO implementa UserDetailsService directamente para evitar conflictos de beans.
 */
@Component
@RequiredArgsConstructor
public class ServicioDetallesCliente {

    private final ServicioAutenticacionCliente servicioAutenticacionCliente;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Parsear username en formato: "cliente:{empresaSlug}:{telefono}"
        if (!username.startsWith("cliente:")) {
            throw new UsernameNotFoundException("Formato de usuario inválido para cliente");
        }

        String[] parts = username.split(":", 3);
        if (parts.length != 3) {
            throw new UsernameNotFoundException("Formato de usuario inválido: " + username);
        }

        String empresaSlug = parts[1];
        String telefono = parts[2];

        // Buscar cliente
        Cliente cliente = servicioAutenticacionCliente.obtenerClienteParaAutenticacion(empresaSlug, telefono);

        if (!cliente.getActivo()) {
            throw new UsernameNotFoundException("Cliente desactivado");
        }

        if (!cliente.getTieneUsuario()) {
            throw new UsernameNotFoundException("Cliente sin cuenta de usuario");
        }

        // Crear UserDetails personalizado con el cliente
        return new ClienteUserDetails(cliente);
    }
}
