package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.ClienteAutenticadoResponse;
import com.example.sitema_de_turnos.dto.publico.RegistroClienteRequest;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.excepcion.ValidacionException;
import com.example.sitema_de_turnos.modelo.Cliente;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.repositorio.RepositorioCliente;
import com.example.sitema_de_turnos.repositorio.RepositorioEmpresa;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para autenticación y registro de clientes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServicioAutenticacionCliente {

    private final RepositorioCliente repositorioCliente;
    private final RepositorioEmpresa repositorioEmpresa;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registra un cliente que previamente reservó como invitado.
     * Actualiza el cliente existente agregando email y contraseña.
     * 
     * @param empresaSlug Slug de la empresa
     * @param request Datos de registro
     * @return Cliente registrado
     * @throws RecursoNoEncontradoException si la empresa o cliente no existen
     * @throws ValidacionException si las contraseñas no coinciden o el cliente ya tiene usuario
     */
    @Transactional
    public ClienteAutenticadoResponse registrarCliente(String empresaSlug, RegistroClienteRequest request) {
        // Validar contraseñas
        if (!request.getContrasena().equals(request.getConfirmarContrasena())) {
            throw new ValidacionException("Las contraseñas no coinciden");
        }

        // Buscar empresa
        Empresa empresa = repositorioEmpresa.findBySlugAndActivaTrue(empresaSlug)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));

        // Buscar cliente invitado por teléfono
        Cliente cliente = repositorioCliente.findByEmpresaAndTelefonoAndActivoTrue(empresa, request.getTelefono())
                .orElseThrow(() -> new ValidacionException(
                    "No se encontró un cliente con ese teléfono. Debe reservar al menos un turno antes de registrarse."
                ));

        // Verificar que no tenga usuario ya
        if (cliente.getTieneUsuario()) {
            throw new ValidacionException("Este cliente ya tiene una cuenta registrada");
        }

        // Verificar que el email no esté en uso por otro cliente de la misma empresa
        repositorioCliente.findByEmpresaAndEmailAndActivoTrue(empresa, request.getEmail())
                .ifPresent(c -> {
                    throw new ValidacionException("Ya existe un cliente con ese email en esta empresa");
                });

        // Actualizar cliente: agregar email y contraseña
        cliente.setEmail(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarEmail(request.getEmail()));
        cliente.setContrasena(passwordEncoder.encode(request.getContrasena()));
        cliente.setTieneUsuario(true);
        
        cliente = repositorioCliente.save(cliente);
        
        log.info("Cliente registrado exitosamente: {} - Empresa: {}", cliente.getTelefono(), empresa.getNombre());

        // Retornar respuesta
        return new ClienteAutenticadoResponse(
            cliente.getId(),
            cliente.getNombre(),
            cliente.getTelefono(),
            cliente.getEmail(),
            empresa.getId(),
            empresa.getNombre()
        );
    }

    /**
     * Busca un cliente por empresa y teléfono para autenticación.
     * 
     * @param empresaSlug Slug de la empresa
     * @param telefono Teléfono del cliente
     * @return Cliente si existe y tiene usuario
     * @throws RecursoNoEncontradoException si no existe
     */
    public Cliente obtenerClienteParaAutenticacion(String empresaSlug, String telefono) {
        Empresa empresa = repositorioEmpresa.findBySlugAndActivaTrue(empresaSlug)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));

        return repositorioCliente.findByEmpresaAndTelefonoAndTieneUsuarioTrueAndActivoTrue(empresa, telefono)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
    }

    /**
     * Verifica si un teléfono tiene cuenta registrada (usuario activo).
     * Método para detección pasiva sin lanzar excepciones.
     * 
     * @param empresaSlug Slug de la empresa
     * @param telefono Teléfono a verificar
     * @return true si el teléfono tiene usuario, false en caso contrario
     */
    public boolean verificarTelefonoTieneUsuario(String empresaSlug, String telefono) {
        try {
            Empresa empresa = repositorioEmpresa.findBySlugAndActivaTrue(empresaSlug)
                    .orElse(null);
            
            if (empresa == null) {
                return false;
            }

            return repositorioCliente.findByEmpresaAndTelefonoAndTieneUsuarioTrueAndActivoTrue(empresa, telefono)
                    .isPresent();
        } catch (Exception e) {
            log.warn("Error verificando teléfono: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si existe un cliente activo con el teléfono en la empresa (sin requerir que tenga usuario).
     */
    public boolean verificarTelefonoExiste(String empresaSlug, String telefono) {
        try {
            Empresa empresa = repositorioEmpresa.findBySlugAndActivaTrue(empresaSlug)
                    .orElse(null);

            if (empresa == null) {
                return false;
            }

            return repositorioCliente.findByEmpresaAndTelefonoAndActivoTrue(empresa, telefono)
                    .isPresent();
        } catch (Exception e) {
            log.warn("Error verificando existencia de teléfono: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtener nombre enmascarado (primer nombre inicial) para mostrar en UI sin exponer datos completos.
     * Retorna null si no hay cliente o no hay nombre.
     */
    public String obtenerNombreEnmascarado(String empresaSlug, String telefono) {
        try {
            Empresa empresa = repositorioEmpresa.findBySlugAndActivaTrue(empresaSlug)
                    .orElse(null);

            if (empresa == null) return null;

            var opt = repositorioCliente.findByEmpresaAndTelefonoAndActivoTrue(empresa, telefono);
            if (opt.isEmpty()) return null;

            String nombre = opt.get().getNombre();
            if (nombre == null || nombre.trim().isEmpty()) return null;
            String[] partes = nombre.trim().split(" ");
            return partes[0].charAt(0) + ".";
        } catch (Exception e) {
            log.warn("Error obteniendo nombre enmascarado: {}", e.getMessage());
            return null;
        }
    }
}
