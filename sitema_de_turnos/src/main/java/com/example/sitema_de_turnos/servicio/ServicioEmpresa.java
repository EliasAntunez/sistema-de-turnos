
package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.*;
import com.example.sitema_de_turnos.modelo.Dueno;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.repositorio.RepositorioDueno;
import com.example.sitema_de_turnos.repositorio.RepositorioEmpresa;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioEmpresa {
    /**
     * Obtener empresa por slug (retorna entidad Empresa, no DTO)
     */
    @Transactional(readOnly = true)
    public Empresa obtenerPorSlug(String slug) {
        return repositorioEmpresa.findBySlugAndActivaTrue(slug).orElse(null);
    }

    private final RepositorioEmpresa repositorioEmpresa;
    private final RepositorioDueno repositorioDueno;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crea una empresa con su dueño en una sola transacción
     * Este método es usado por el SuperAdmin para dar de alta empresas
     */
    @Transactional
    public EmpresaResponse crearEmpresaConDueno(RegistroEmpresaConDuenoRequest request) {
        // Validaciones de negocio
        validarDatosEmpresa(request.getEmpresa());
        validarDatosDueno(request.getDueno());

        // 1. Crear el Dueño primero
        Dueno dueno = new Dueno();
        dueno.setNombre(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(request.getDueno().getNombre()));
        dueno.setApellido(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(request.getDueno().getApellido()));
        dueno.setEmail(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarEmail(request.getDueno().getEmail()));
        dueno.setContrasena(passwordEncoder.encode(request.getDueno().getContrasena()));
        dueno.setTelefono(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarTelefono(request.getDueno().getTelefono()));
        dueno.setDocumento(request.getDueno().getDocumento());
        dueno.setTipoDocumento(request.getDueno().getTipoDocumento());
        dueno.setActivo(true);
        
        // El rol se asigna automáticamente en @PrePersist
        dueno = repositorioDueno.save(dueno);

        // 2. Crear la Empresa asociada al Dueño
        Empresa empresa = new Empresa();
        empresa.setNombre(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(request.getEmpresa().getNombre()));
        empresa.setSlug(request.getEmpresa().getSlug());
        empresa.setDescripcion(request.getEmpresa().getDescripcion());
        empresa.setCuit(request.getEmpresa().getCuit());
        empresa.setDireccion(request.getEmpresa().getDireccion());
        empresa.setCiudad(request.getEmpresa().getCiudad());
        empresa.setProvincia(request.getEmpresa().getProvincia());
        empresa.setTelefono(request.getEmpresa().getTelefono());
        empresa.setEmail(request.getEmpresa().getEmail());
        empresa.setDiasMaximosReserva(request.getEmpresa().getDiasMaximosReserva() != null 
                ? request.getEmpresa().getDiasMaximosReserva() 
                : 30); // Default 30 días
        empresa.setActiva(true);
        empresa.setDueno(dueno);

        empresa = repositorioEmpresa.save(empresa);

        // 3. Mapear a Response
        return mapearAEmpresaResponse(empresa);
    }

    /**
     * Obtener todas las empresas (para vista de SuperAdmin)
     */
    @Transactional(readOnly = true)
    public java.util.List<EmpresaResponse> obtenerTodasLasEmpresas() {
        return repositorioEmpresa.findAll().stream()
                .map(this::mapearAEmpresaResponse)
                .toList();
    }

    /**
     * Obtener empresa por ID
     */
    @Transactional(readOnly = true)
    public EmpresaResponse obtenerEmpresaPorId(Long id) {
        Empresa empresa = repositorioEmpresa.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));
        return mapearAEmpresaResponse(empresa);
    }

    /**
     * Obtener empresa por ID (retorna entidad Empresa, no DTO)
     */
    @Transactional(readOnly = true)
    public Empresa obtenerPorId(Long id) {
        return repositorioEmpresa.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));
    }

    /**
     * Activar/Desactivar empresa
     * Solo SuperAdmin puede realizar esta operación
     */
    @Transactional
    public void cambiarEstadoEmpresa(Long id, Boolean activa) {
        Empresa empresa = repositorioEmpresa.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));
        
        // Si se quiere desactivar, validar que no sea la única empresa activa
        if (!activa) {
            long empresasActivas = repositorioEmpresa.countByActiva(true);
            if (empresasActivas <= 1) {
                throw new RuntimeException("No se puede desactivar la única empresa activa del sistema");
            }
        }
        
        empresa.setActiva(activa);
        repositorioEmpresa.save(empresa);
    }

    // ===================== VALIDACIONES =====================

    private void validarDatosEmpresa(RegistroEmpresaRequest empresaRequest) {
        // Validar unicidad del CUIT (crítico - documento fiscal único en Argentina)
        if (repositorioEmpresa.existsByCuit(empresaRequest.getCuit())) {
            throw new RuntimeException("Ya existe una empresa registrada con ese CUIT");
        }
        
        // Validar unicidad del slug
        if (repositorioEmpresa.findBySlugAndActivaTrue(empresaRequest.getSlug()).isPresent()) {
            throw new RuntimeException("Ya existe una empresa activa con ese slug");
        }
    }

    // Agregá este método en tu servicio de Empresa
    public Empresa obtenerPorDueno(Long duenoId) {
        return repositorioEmpresa.findByDuenoId(duenoId).orElse(null);
    }

    private void validarDatosDueno(RegistroDuenoRequest duenoRequest) {
        // Verificar ambas condiciones sin revelar cuál falló (seguridad)
        boolean emailExiste = repositorioDueno.existsByEmail(duenoRequest.getEmail());
        boolean documentoExiste = repositorioDueno.existsByDocumento(duenoRequest.getDocumento());
        
        if (emailExiste || documentoExiste) {
            throw new RuntimeException("Los datos del dueño proporcionados no son válidos o ya están registrados");
        }
    }

    // ===================== MAPPERS =====================

    private EmpresaResponse mapearAEmpresaResponse(Empresa empresa) {
        EmpresaResponse response = new EmpresaResponse();
        response.setId(empresa.getId());
        response.setNombre(empresa.getNombre());
        response.setDescripcion(empresa.getDescripcion());
        response.setCuit(empresa.getCuit());
        response.setDireccion(empresa.getDireccion());
        response.setCiudad(empresa.getCiudad());
        response.setProvincia(empresa.getProvincia());
        response.setTelefono(empresa.getTelefono());
        response.setEmail(empresa.getEmail());
        response.setActiva(empresa.getActiva());
        response.setFechaCreacion(empresa.getFechaCreacion());
        
        // Mapear dueño
        if (empresa.getDueno() != null) {
            response.setDueno(mapearADuenoResponse(empresa.getDueno()));
        }
        
        return response;
    }

    private DuenoResponse mapearADuenoResponse(Dueno dueno) {
        DuenoResponse response = new DuenoResponse();
        response.setId(dueno.getId());
        response.setNombre(dueno.getNombre());
        response.setApellido(dueno.getApellido());
        response.setEmail(dueno.getEmail());
        response.setTelefono(dueno.getTelefono());
        response.setDocumento(dueno.getDocumento());
        response.setTipoDocumento(dueno.getTipoDocumento());
        return response;
    }
}
