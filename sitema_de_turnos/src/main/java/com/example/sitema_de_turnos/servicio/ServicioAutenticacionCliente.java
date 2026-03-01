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

import java.util.Optional;

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
     * Registra un nuevo cliente con cuenta de usuario.
     * Crea un cliente nuevo con nombreUsuario, email y contraseña.
     * 
     * @param empresaSlug Slug de la empresa
     * @param request Datos de registro
     * @return Cliente registrado
     * @throws RecursoNoEncontradoException si la empresa no existe
     * @throws ValidacionException si las contraseñas no coinciden o ya existe el email/nombreUsuario
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

        String emailNormalizado = com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarEmail(request.getEmail());
        
        // Buscar si existe un cliente con ese email
        Optional<Cliente> clienteExistente = repositorioCliente.findByEmpresaAndEmailAndActivoTrue(empresa, emailNormalizado);
        
        // Si existe un cliente con ese email
        if (clienteExistente.isPresent()) {
            Cliente cliente = clienteExistente.get();
            
            // Si ya tiene usuario (cuenta registrada), no permitir duplicado
            if (Boolean.TRUE.equals(cliente.getTieneUsuario())) {
                throw new ValidacionException("Ya existe un cliente con ese email en esta empresa");
            }
            
            // Es un cliente invitado -> convertir a usuario registrado
            log.info("Convirtiendo cliente invitado a usuario registrado: {} - Empresa: {}", emailNormalizado, empresa.getNombre());
            
            // Verificar que el nombre de usuario no esté en uso por OTRO cliente
            Optional<Cliente> otroClienteConNombre = repositorioCliente.findByEmpresaAndNombreUsuarioAndActivoTrue(empresa, request.getNombreUsuario());
            if (otroClienteConNombre.isPresent() && !otroClienteConNombre.get().getId().equals(cliente.getId())) {
                throw new ValidacionException("Ya existe un cliente con ese nombre de usuario en esta empresa");
            }
            
            // Verificar teléfono si se proporciona (que no lo use OTRO cliente)
            if (request.getTelefono() != null && !request.getTelefono().trim().isEmpty()) {
                if (!request.getTelefono().matches("^[+]?[0-9\\s\\-()]{8,20}$")) {
                    throw new ValidacionException("Formato de teléfono inválido");
                }
                Optional<Cliente> otroClienteConTelefono = repositorioCliente.findByEmpresaAndTelefonoAndActivoTrue(empresa, request.getTelefono());
                if (otroClienteConTelefono.isPresent() && !otroClienteConTelefono.get().getId().equals(cliente.getId())) {
                    throw new ValidacionException("Ya existe un cliente con ese teléfono en esta empresa");
                }
            }
            
            // Actualizar cliente invitado a usuario registrado
            cliente.setNombreUsuario(request.getNombreUsuario());
            cliente.setContrasena(passwordEncoder.encode(request.getContrasena()));
            cliente.setTieneUsuario(true);
            
            // Actualizar teléfono si se proporciona
            if (request.getTelefono() != null && !request.getTelefono().trim().isEmpty()) {
                cliente.setTelefono(request.getTelefono());
            }
            
            cliente = repositorioCliente.save(cliente);
            
            log.info("Cliente invitado convertido exitosamente a usuario registrado: {} - Empresa: {} (Turnos anteriores asociados automáticamente)", 
                    cliente.getEmail(), empresa.getNombre());
            
            return new ClienteAutenticadoResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getTelefono(),
                cliente.getEmail(),
                empresa.getId(),
                empresa.getNombre()
            );
        }
        
        // No existe cliente con ese email -> crear nuevo usuario registrado
        
        // Verificar que el nombre de usuario no esté en uso
        if (repositorioCliente.existsByEmpresaAndNombreUsuarioAndActivoTrue(empresa, request.getNombreUsuario())) {
            throw new ValidacionException("Ya existe un cliente con ese nombre de usuario en esta empresa");
        }

        // Verificar teléfono si se proporciona
        if (request.getTelefono() != null && !request.getTelefono().trim().isEmpty()) {
            // Validar formato del teléfono
            if (!request.getTelefono().matches("^[+]?[0-9\\s\\-()]{8,20}$")) {
                throw new ValidacionException("Formato de teléfono inválido");
            }
            if (repositorioCliente.existsByEmpresaAndTelefonoAndActivoTrue(empresa, request.getTelefono())) {
                throw new ValidacionException("Ya existe un cliente con ese teléfono en esta empresa");
            }
        }

        // Crear nuevo cliente registrado
        Cliente cliente = new Cliente();
        cliente.setEmpresa(empresa);
        cliente.setNombre(request.getNombreUsuario()); // Usamos nombreUsuario como nombre inicial
        cliente.setNombreUsuario(request.getNombreUsuario());
        cliente.setEmail(emailNormalizado);
        cliente.setContrasena(passwordEncoder.encode(request.getContrasena()));
        cliente.setTieneUsuario(true);
        cliente.setTelefonoValidado(false);
        cliente.setActivo(true);
        
        // Agregar teléfono si se proporciona
        if (request.getTelefono() != null && !request.getTelefono().trim().isEmpty()) {
            cliente.setTelefono(request.getTelefono());
        }
        
        cliente = repositorioCliente.save(cliente);
        
        log.info("Cliente registrado exitosamente: {} - Empresa: {}", cliente.getEmail(), empresa.getNombre());

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
     * Busca un cliente por empresa y email/nombreUsuario para autenticación.
     * 
     * @param empresaSlug Slug de la empresa
     * @param identificador Email o nombre de usuario del cliente
     * @return Cliente si existe y tiene usuario
     * @throws RecursoNoEncontradoException si no existe
     */
    public Cliente obtenerClienteParaAutenticacion(String empresaSlug, String identificador) {
        log.info("🔎 [AUTH-CLIENTE] Buscando cliente - EmpresaSlug: {} | Identificador: {}", empresaSlug, identificador);
        Empresa empresa = repositorioEmpresa.findBySlugAndActivaTrue(empresaSlug)
                .orElseThrow(() -> {
                    log.error("❌ [AUTH-CLIENTE] Empresa no encontrada o inactiva - Slug: {}", empresaSlug);
                    return new RecursoNoEncontradoException("Empresa no encontrada");
                });
        log.info("📍 [AUTH-CLIENTE] Empresa encontrada - ID: {} | Nombre: {}", empresa.getId(), empresa.getNombre());

        Optional<Cliente> clienteOpt = repositorioCliente.findByEmpresaAndEmailOrNombreUsuarioAndTieneUsuarioTrueAndActivoTrue(empresa, identificador);
        if (clienteOpt.isEmpty()) {
            log.error("❌ [AUTH-CLIENTE] Cliente no encontrado - EmpresaID: {} | Identificador: {} | Criterio: activo=true AND tieneUsuario=true AND (email='{}' OR nombreUsuario='{}')", 
                    empresa.getId(), identificador, identificador, identificador);
            throw new RecursoNoEncontradoException("Cliente no encontrado");
        }
        Cliente cliente = clienteOpt.get();
        log.info("✅ [AUTH-CLIENTE] Cliente encontrado - ID: {} | Nombre: {} | Email: {} | NombreUsuario: {} | TieneUsuario: {} | Activo: {}", 
                cliente.getId(), cliente.getNombre(), cliente.getEmail(), cliente.getNombreUsuario(), 
                cliente.getTieneUsuario(), cliente.getActivo());
        return cliente;
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
