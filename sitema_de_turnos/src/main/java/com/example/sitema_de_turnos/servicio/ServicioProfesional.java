package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.ProfesionalResponse;
import com.example.sitema_de_turnos.dto.ProfesionalServicioResponse;
import com.example.sitema_de_turnos.dto.RegistroProfesionalRequest;
import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;
import com.example.sitema_de_turnos.excepcion.ConflictoException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.mapper.ProfesionalMapper;
import com.example.sitema_de_turnos.modelo.Dueno;
import com.example.sitema_de_turnos.modelo.EstadoTurno;
import com.example.sitema_de_turnos.modelo.Profesional;
import com.example.sitema_de_turnos.modelo.ProfesionalServicio;
import com.example.sitema_de_turnos.modelo.RolUsuario;
import com.example.sitema_de_turnos.modelo.Servicio;
import com.example.sitema_de_turnos.repositorio.RepositorioProfesional;
import com.example.sitema_de_turnos.repositorio.RepositorioProfesionalServicio;
import com.example.sitema_de_turnos.repositorio.RepositorioServicio;
import com.example.sitema_de_turnos.repositorio.RepositorioTurno;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioProfesional {

    private final RepositorioProfesional repositorioProfesional;
    private final RepositorioProfesionalServicio repositorioProfesionalServicio;
    private final RepositorioServicio repositorioServicio;
    private final RepositorioTurno repositorioTurno;
    private final PasswordEncoder passwordEncoder;
    private final ServicioValidacionDueno servicioValidacionDueno;

    @Transactional(readOnly = true)
    public List<ProfesionalResponse> obtenerProfesionalesPorEmpresa(String emailDueno) {
        Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);
        
        List<Profesional> profesionales = repositorioProfesional.findByEmpresa(dueno.getEmpresa());
        return profesionales.stream()
                .map(ProfesionalMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProfesionalResponse crearProfesional(String emailDueno, RegistroProfesionalRequest dto) {
        // Validar dueño activo y empresa activa
        Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);

        // Validar email único
        if (repositorioProfesional.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El email " + dto.getEmail() + " ya está registrado");
        }

        // Validar contraseña
        if (dto.getContrasena() == null || dto.getContrasena().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria al crear un profesional");
        }
        if (dto.getContrasena().length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }

        // Crear profesional
        Profesional profesional = new Profesional();
        profesional.setNombre(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(dto.getNombre()));
        profesional.setApellido(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(dto.getApellido()));
        profesional.setEmail(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarEmail(dto.getEmail()));
        profesional.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        profesional.setTelefono(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarTelefono(dto.getTelefono()));
        profesional.setDescripcion(dto.getDescripcion());
        profesional.setRol(RolUsuario.PROFESIONAL);
        profesional.setActivo(true);
        profesional.setEmpresa(dueno.getEmpresa()); // Usa la empresa del dueño autenticado
        profesional.setFechaCreacion(LocalDateTime.now());
        profesional.setFechaActualizacion(LocalDateTime.now());

        Profesional guardado = repositorioProfesional.save(profesional);
        return ProfesionalMapper.toResponse(guardado);
    }

    @Transactional
    public ProfesionalResponse actualizarProfesional(String emailDueno, Long idProfesional, RegistroProfesionalRequest dto) {
        // Validar dueño activo y empresa activa
        Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);

        // Buscar profesional
        Profesional profesional = repositorioProfesional.findById(idProfesional)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        // Validar que el profesional pertenece a la empresa del dueño
        if (!profesional.getEmpresa().getId().equals(dueno.getEmpresa().getId())) {
            throw new AccesoDenegadoException("No tiene permiso para modificar este profesional");
        }

        // Validar email único (si cambió)
        if (!profesional.getEmail().equalsIgnoreCase(dto.getEmail())) {
            String emailNormalizado = com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarEmail(dto.getEmail());
            if (repositorioProfesional.findByEmail(emailNormalizado).isPresent()) {
                throw new IllegalArgumentException("El email " + dto.getEmail() + " ya está registrado");
            }
            profesional.setEmail(emailNormalizado);
        }

        // Actualizar datos
        profesional.setNombre(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(dto.getNombre()));
        profesional.setApellido(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(dto.getApellido()));
        profesional.setTelefono(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarTelefono(dto.getTelefono()));
        profesional.setDescripcion(dto.getDescripcion());
        
        // Solo actualizar contraseña si se proporciona una nueva
        if (dto.getContrasena() != null && !dto.getContrasena().isEmpty()) {
            if (dto.getContrasena().length() < 8) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
            }
            profesional.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        }
        
        profesional.setFechaActualizacion(LocalDateTime.now());

        Profesional actualizado = repositorioProfesional.save(profesional);
        
        return ProfesionalMapper.toResponse(actualizado);
    }

    @Transactional
    public void eliminarProfesional(String emailDueno, Long idProfesional) {
        desactivarProfesional(emailDueno, idProfesional);
    }

    @Transactional
    public void activarProfesional(String emailDueno, Long idProfesional) {
        cambiarEstadoProfesional(emailDueno, idProfesional, true);
    }

    @Transactional
    public void desactivarProfesional(String emailDueno, Long idProfesional) {
        cambiarEstadoProfesional(emailDueno, idProfesional, false);
    }

    private void cambiarEstadoProfesional(String emailDueno, Long idProfesional, boolean activo) {
        // Validar dueño activo y empresa activa
        Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);

        // Buscar profesional
        Profesional profesional = repositorioProfesional.findById(idProfesional)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        // Validar que el profesional pertenece a la empresa del dueño
        if (!profesional.getEmpresa().getId().equals(dueno.getEmpresa().getId())) {
            throw new AccesoDenegadoException("No tiene permiso para modificar este profesional");
        }

        // Si se está desactivando, validar que no tenga turnos activos
        if (!activo) {
            validarSinTurnosActivos(profesional);
        }

        // Cambiar estado
        profesional.setActivo(activo);
        profesional.setFechaActualizacion(LocalDateTime.now());
        repositorioProfesional.save(profesional);
    }

    /**
     * Valida que el profesional no tenga turnos activos antes de desactivarlo.
     * 
     * Se consideran turnos activos aquellos en los estados:
     * - CREADO: Turno creado, esperando confirmación inicial
     * - PENDIENTE_CONFIRMACION: Esperando confirmación del profesional/empresa
     * - CONFIRMADO: Turno confirmado y comprometido
     * 
     * @param profesional Profesional a validar
     * @throws ConflictoException si el profesional tiene turnos activos
     */
    private void validarSinTurnosActivos(Profesional profesional) {
        List<EstadoTurno> estadosActivos = Arrays.asList(
            EstadoTurno.CREADO,
            EstadoTurno.PENDIENTE_CONFIRMACION,
            EstadoTurno.CONFIRMADO
        );
        
        long cantidadTurnosActivos = repositorioTurno.countByProfesionalAndEstadoIn(profesional, estadosActivos);
        
        if (cantidadTurnosActivos > 0) {
            throw new ConflictoException(
                String.format("No se puede desactivar el profesional %s %s porque tiene %d turno(s) activo(s) " +
                             "(en estado CREADO, PENDIENTE_CONFIRMACION o CONFIRMADO). " +
                             "Debe cancelar o completar estos turnos antes de desactivar al profesional.",
                             profesional.getNombre(), profesional.getApellido(), cantidadTurnosActivos)
            );
        }
    }

    /**
     * Obtiene todos los servicios disponibles para un profesional.
     * Lógica: Todos los servicios activos de la empresa, indicando cuáles están habilitados
     * explícitamente en la tabla profesional_servicio.
     */
    @Transactional(readOnly = true)
    public List<ProfesionalServicioResponse> obtenerServiciosDeProfesional(String emailDueno, Long profesionalId) {
        Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);
        
        Profesional profesional = repositorioProfesional.findById(profesionalId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));
        
        // Validar que el profesional pertenece a la empresa del dueño
        if (!profesional.getEmpresa().getId().equals(dueno.getEmpresa().getId())) {
            throw new AccesoDenegadoException("No tiene permiso para acceder a este profesional");
        }
        
        // Obtener todos los servicios activos de la empresa
        List<Servicio> servicios = repositorioServicio.findByEmpresaAndActivoTrue(dueno.getEmpresa());
        
        // Obtener habilitaciones explícitas (profesional_servicio con activo=true)
        List<ProfesionalServicio> habilitaciones = repositorioProfesionalServicio.findByProfesionalAndActivoTrue(profesional);
        Set<Long> serviciosHabilitados = habilitaciones.stream()
                .map(ps -> ps.getServicio().getId())
                .collect(Collectors.toSet());
        
        List<ProfesionalServicioResponse> resultado = new ArrayList<>();
        
        for (Servicio servicio : servicios) {
            ProfesionalServicioResponse response = new ProfesionalServicioResponse();
            response.setServicioId(servicio.getId());
            response.setNombre(servicio.getNombre());
            response.setDescripcion(servicio.getDescripcion());
            response.setDuracionMinutos(servicio.getDuracionMinutos());
            response.setPrecio(servicio.getPrecio().doubleValue());
            
            // El servicio está disponible si tiene habilitación explícita
            boolean disponible = serviciosHabilitados.contains(servicio.getId());
            response.setDisponible(disponible);
            
            resultado.add(response);
        }
        
        return resultado;
    }

    /**
     * Activa o desactiva un servicio específico para un profesional
     * Crea un override en la tabla profesional_servicio
     */
    @Transactional
    public void toggleServicioProfesional(String emailDueno, Long profesionalId, Long servicioId, Boolean disponible) {
        Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);
        
        Profesional profesional = repositorioProfesional.findById(profesionalId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));
        
        // Validar que el profesional pertenece a la empresa del dueño
        if (!profesional.getEmpresa().getId().equals(dueno.getEmpresa().getId())) {
            throw new AccesoDenegadoException("No tiene permiso para modificar este profesional");
        }
        
        Servicio servicio = repositorioServicio.findById(servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado"));
        
        // Validar que el servicio pertenece a la empresa
        if (!servicio.getEmpresa().getId().equals(dueno.getEmpresa().getId())) {
            throw new AccesoDenegadoException("No tiene permiso para acceder a este servicio");
        }
        
        // MEJORA-005: Validar que el servicio esté activo
        if (!servicio.getActivo()) {
            throw new IllegalArgumentException(
                "No se puede modificar la disponibilidad de un servicio desactivado. " +
                "Active el servicio primero desde la gestión de servicios."
            );
        }
        
        // Buscar o crear habilitación
        Optional<ProfesionalServicio> existente = repositorioProfesionalServicio
                .findByProfesionalAndServicio(profesional, servicio);
        
        if (disponible) {
            // Si se activa y no existe, crear habilitación
            if (existente.isEmpty()) {
                ProfesionalServicio ps = new ProfesionalServicio();
                ps.setProfesional(profesional);
                ps.setServicio(servicio);
                ps.setActivo(true);
                repositorioProfesionalServicio.save(ps);
            } else {
                // Si existe, actualizar a activo
                ProfesionalServicio ps = existente.get();
                ps.setActivo(true);
                repositorioProfesionalServicio.save(ps);
            }
        } else {
            // Si se desactiva, eliminar habilitación
            existente.ifPresent(repositorioProfesionalServicio::delete);
        }
    }
}