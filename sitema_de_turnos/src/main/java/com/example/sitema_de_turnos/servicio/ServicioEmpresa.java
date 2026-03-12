package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.*;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.PerfilDueno;
import com.example.sitema_de_turnos.modelo.PerfilProfesional;
import com.example.sitema_de_turnos.modelo.RolUsuario;
import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.repositorio.RepositorioEmpresa;
import com.example.sitema_de_turnos.repositorio.RepositorioPerfilDueno;
import com.example.sitema_de_turnos.repositorio.RepositorioPerfilProfesional;
import com.example.sitema_de_turnos.repositorio.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ServicioEmpresa {
    
    private final RepositorioEmpresa repositorioEmpresa;
    private final RepositorioPerfilDueno repositorioPerfilDueno;
    private final RepositorioPerfilProfesional repositorioPerfilProfesional;
    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder passwordEncoder;
    private final ServicioDueno servicioDueno;

    /**
     * Obtener empresa por slug (retorna entidad Empresa, no DTO)
     */
    @Transactional(readOnly = true)
    public Empresa obtenerPorSlug(String slug) {
        return repositorioEmpresa.findBySlugAndActivaTrue(slug).orElse(null);
    }

    /**
     * Crea una empresa con su dueño en una sola transacción
     * Este método es usado por el SuperAdmin para dar de alta empresas
     */
    @Transactional
    public EmpresaResponse crearEmpresaConDueno(RegistroEmpresaConDuenoRequest request) {
        validarDatosEmpresa(request.getEmpresa());
        validarDatosDueno(request.getDueno());

        // 1. Crear el Usuario con rol DUENO
        Usuario usuario = new Usuario();
        usuario.setNombre(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(request.getDueno().getNombre()));
        usuario.setApellido(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(request.getDueno().getApellido()));
        usuario.setEmail(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarEmail(request.getDueno().getEmail()));
        usuario.setContrasena(passwordEncoder.encode(request.getDueno().getContrasena()));
        usuario.setTelefono(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarTelefono(request.getDueno().getTelefono()));
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuario.getRoles().add(RolUsuario.DUENO);
        usuario = repositorioUsuario.save(usuario);

        // 2. Crear el PerfilDueno
        PerfilDueno perfilDueno = new PerfilDueno();
        perfilDueno.setUsuario(usuario);
        perfilDueno.setDocumento(request.getDueno().getDocumento());
        perfilDueno.setTipoDocumento(request.getDueno().getTipoDocumento());
        perfilDueno.setActivo(true);
        perfilDueno = repositorioPerfilDueno.save(perfilDueno);

        // 3. Crear la Empresa asociada al PerfilDueno
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
                : 30);
        empresa.setActiva(true);
        empresa.setPerfilDueno(perfilDueno);

        empresa = repositorioEmpresa.save(empresa);

        // 4. Opcionalmente crear PerfilProfesional para el dueño (doble rol)
        if (request.isCrearPerfilProfesional()) {
            usuario.getRoles().add(RolUsuario.PROFESIONAL);
            repositorioUsuario.save(usuario);

            PerfilProfesional perfilProfesional = new PerfilProfesional();
            perfilProfesional.setUsuario(usuario);
            perfilProfesional.setEmpresa(empresa);
            perfilProfesional.setActivo(true);
            repositorioPerfilProfesional.save(perfilProfesional);
        }

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
    public Empresa obtenerPorDueno(Long perfilDuenoId) {
        return repositorioEmpresa.findByPerfilDuenoId(perfilDuenoId).orElse(null);
    }

    private void validarDatosDueno(RegistroDuenoRequest duenoRequest) {
        boolean emailExiste = repositorioUsuario.existsByEmail(duenoRequest.getEmail());
        boolean documentoExiste = repositorioPerfilDueno.existsByDocumento(duenoRequest.getDocumento());

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

        if (empresa.getPerfilDueno() != null) {
            response.setDueno(mapearADuenoResponse(empresa.getPerfilDueno()));
        }

        return response;
    }

    private DuenoResponse mapearADuenoResponse(PerfilDueno perfil) {
        DuenoResponse response = new DuenoResponse();
        response.setId(perfil.getId());
        response.setNombre(perfil.getUsuario().getNombre());
        response.setApellido(perfil.getUsuario().getApellido());
        response.setEmail(perfil.getUsuario().getEmail());
        response.setTelefono(perfil.getUsuario().getTelefono());
        response.setDocumento(perfil.getDocumento());
        response.setTipoDocumento(perfil.getTipoDocumento());
        return response;
    }

    // ===================== GESTIÓN DE CONFIGURACIÓN =====================

    @Transactional(readOnly = true)
    public ConfiguracionEmpresaResponse obtenerConfiguracion(String emailDueno) {
        PerfilDueno perfil = servicioDueno.obtenerPorEmail(emailDueno);
        Empresa empresa = perfil.getEmpresa();

        if (empresa == null) {
            throw new RuntimeException("El dueño no tiene empresa asociada");
        }

        return mapearAConfiguracionResponse(empresa);
    }

    @Transactional
    public ConfiguracionEmpresaResponse actualizarConfiguracion(
            String emailDueno,
            ActualizarConfiguracionEmpresaRequest request) {

        PerfilDueno perfil = servicioDueno.obtenerPorEmail(emailDueno);
        Empresa empresa = perfil.getEmpresa();

        if (empresa == null) {
            throw new RuntimeException("El dueño no tiene empresa asociada");
        }
        
        // Actualizar configuración operativa
        empresa.setBufferPorDefecto(request.getBufferPorDefecto());
        empresa.setTiempoMinimoAnticipacionMinutos(request.getTiempoMinimoAnticipacionMinutos());
        empresa.setDiasMaximosReserva(request.getDiasMaximosReserva());
        
        // Actualizar configuración de recordatorios
        empresa.setHorasAntesRecordatorio(request.getHorasAntesRecordatorio());
        empresa.setEnviarRecordatorios(request.getEnviarRecordatorios());
        
        empresa = repositorioEmpresa.save(empresa);
        
        return mapearAConfiguracionResponse(empresa);
    }

    /**
     * Mapear entidad Empresa a ConfiguracionEmpresaResponse
     */
    private ConfiguracionEmpresaResponse mapearAConfiguracionResponse(Empresa empresa) {
        ConfiguracionEmpresaResponse response = new ConfiguracionEmpresaResponse();
        response.setBufferPorDefecto(empresa.getBufferPorDefecto());
        response.setTiempoMinimoAnticipacionMinutos(empresa.getTiempoMinimoAnticipacionMinutos());
        response.setDiasMaximosReserva(empresa.getDiasMaximosReserva());
        response.setTimezone(empresa.getTimezone());
        response.setHorasAntesRecordatorio(empresa.getHorasAntesRecordatorio());
        response.setEnviarRecordatorios(empresa.getEnviarRecordatorios());
        return response;
    }
}
