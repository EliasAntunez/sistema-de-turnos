package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.modelo.Cliente;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Servicio auxiliar para cargar detalles de clientes.
 * 
 * IMPORTANTE: Este servicio se usa SOLO cuando el username sigue el formato:
 * "cliente:{empresaSlug}:{emailOrNombreUsuario}"
 * 
 * Esto permite diferenciar entre:
 * - Usuarios del sistema (Usuario entity): email directo
 * - Clientes (Cliente entity): prefijo "cliente:"
 * 
 * NO implementa UserDetailsService directamente para evitar conflictos de beans.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ServicioDetallesCliente {

    private final ServicioAutenticacionCliente servicioAutenticacionCliente;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("🔍 [DETALLES-CLIENTE] Cargando UserDetails para: {}", username);
        // Parsear username en formato: "cliente:{empresaSlug}:{emailOrNombreUsuario}"
        if (!username.startsWith("cliente:")) {
            log.error("❌ [DETALLES-CLIENTE] Formato inválido, no empieza con 'cliente:': {}", username);
            throw new UsernameNotFoundException("Formato de usuario inválido para cliente");
        }

        String[] parts = username.split(":", 3);
        if (parts.length != 3) {
            log.error("❌ [DETALLES-CLIENTE] Formato inválido, partes incorrectas: {}", username);
            throw new UsernameNotFoundException("Formato de usuario inválido: " + username);
        }

        String empresaSlug = parts[1];
        String identificador = parts[2]; // Email o nombre de usuario
        log.info("📋 [DETALLES-CLIENTE] Parseado - EmpresaSlug: {} | Identificador: {}", empresaSlug, identificador);

        // Buscar cliente
        Cliente cliente = servicioAutenticacionCliente.obtenerClienteParaAutenticacion(empresaSlug, identificador);
        log.info("✅ [DETALLES-CLIENTE] Cliente encontrado - ID: {} | Nombre: {} | TieneUsuario: {} | Activo: {}", 
                cliente.getId(), cliente.getNombre(), cliente.getTieneUsuario(), cliente.getActivo());

        if (!cliente.getActivo()) {
            log.error("❌ [DETALLES-CLIENTE] Cliente desactivado - ID: {}", cliente.getId());
            throw new UsernameNotFoundException("Cliente desactivado");
        }

        if (!cliente.getTieneUsuario()) {
            log.error("❌ [DETALLES-CLIENTE] Cliente sin cuenta de usuario - ID: {} | TieneUsuario: {}", 
                    cliente.getId(), cliente.getTieneUsuario());
            throw new UsernameNotFoundException("Cliente sin cuenta de usuario");
        }

        // Crear UserDetails personalizado con el cliente
        log.info("✅ [DETALLES-CLIENTE] UserDetails creado exitosamente para cliente ID: {}", cliente.getId());
        return new ClienteUserDetails(cliente);
    }
}
