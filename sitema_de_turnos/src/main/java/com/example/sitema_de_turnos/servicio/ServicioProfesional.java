package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.ProfesionalResponse;
import com.example.sitema_de_turnos.dto.ProfesionalServicioResponse;
import com.example.sitema_de_turnos.dto.RegistroProfesionalRequest;
import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.mapper.ProfesionalMapper;
import com.example.sitema_de_turnos.modelo.Dueno;
import com.example.sitema_de_turnos.modelo.Especialidad;
import com.example.sitema_de_turnos.modelo.Profesional;
import com.example.sitema_de_turnos.modelo.ProfesionalServicio;
import com.example.sitema_de_turnos.modelo.RolUsuario;
import com.example.sitema_de_turnos.modelo.Servicio;
import com.example.sitema_de_turnos.repositorio.RepositorioEspecialidad;
import com.example.sitema_de_turnos.repositorio.RepositorioProfesional;
import com.example.sitema_de_turnos.repositorio.RepositorioProfesionalServicio;
import com.example.sitema_de_turnos.repositorio.RepositorioServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("removal")
public class ServicioProfesional {

    private final RepositorioProfesional repositorioProfesional;
    private final RepositorioEspecialidad repositorioEspecialidad;
    private final RepositorioProfesionalServicio repositorioProfesionalServicio;
    private final RepositorioServicio repositorioServicio;
    private final PasswordEncoder passwordEncoder;
    private final ServicioValidacionDueno servicioValidacionDueno;

    @Transactional(readOnly = true)
    public List<ProfesionalResponse> obtenerProfesionalesPorEmpresa(String emailDueno) {
        Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);
        
        List<Profesional> profesionales = repositorioProfesional.findByEmpresaWithEspecialidades(dueno.getEmpresa());
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

        // Obtener o crear especialidades
        Set<Especialidad> especialidades = obtenerOCrearEspecialidades(dto.getEspecialidades());
        
        // MEJORA-003: Validar que todas las especialidades estén activas
        validarEspecialidadesActivas(especialidades);

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
        profesional.setEspecialidades(especialidades);
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

        // Buscar profesional con especialidades
        Profesional profesional = repositorioProfesional.findByIdWithEspecialidades(idProfesional)
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

        // Guardar especialidades anteriores para limpiar overrides huérfanos
        Set<Especialidad> especialidadesAnteriores = new HashSet<>(profesional.getEspecialidades());

        // Obtener nuevas especialidades
        Set<Especialidad> especialidadesNuevas = obtenerOCrearEspecialidades(dto.getEspecialidades());
        
        // MEJORA-003: Validar que todas las especialidades estén activas
        validarEspecialidadesActivas(especialidadesNuevas);

        // Actualizar datos
        profesional.setNombre(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(dto.getNombre()));
        profesional.setApellido(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(dto.getApellido()));
        profesional.setTelefono(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarTelefono(dto.getTelefono()));
        profesional.setEspecialidades(especialidadesNuevas);
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
        
        // HERENCIA-002: Limpiar overrides huérfanos si cambiaron las especialidades
        if (!especialidadesAnteriores.equals(especialidadesNuevas)) {
            limpiarOverridesHuerfanos(actualizado);
        }
        
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

        // Cambiar estado
        profesional.setActivo(activo);
        profesional.setFechaActualizacion(LocalDateTime.now());
        repositorioProfesional.save(profesional);
    }

    /**
     * Valida que las especialidades existan en el sistema
     * Si alguna no existe, lanza una excepción para que el dueño contacte al administrador
     */
    private Set<Especialidad> obtenerOCrearEspecialidades(List<String> nombresEspecialidades) {
        Set<Especialidad> especialidades = new HashSet<>();
        
        for (String nombre : nombresEspecialidades) {
            String nombreNormalizado = nombre.trim();
            Especialidad especialidad = repositorioEspecialidad.findByNombreIgnoreCase(nombreNormalizado)
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Especialidad '" + nombreNormalizado + "' no encontrada. " +
                            "Contacte al administrador para darla de alta en el sistema."
                    ));
            
            // Verificar que la especialidad esté activa
            if (!especialidad.getActiva()) {
                throw new RecursoNoEncontradoException(
                        "La especialidad '" + nombreNormalizado + "' está desactivada. " +
                        "Contacte al administrador."
                );
            }
            
            especialidades.add(especialidad);
        }
        
        return especialidades;
    }

    /**
     * Obtiene todos los servicios disponibles para un profesional
     * Lógica: Servicios que comparten al menos una especialidad con el profesional
     * Indica si están activos o desactivados manualmente
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
        List<Servicio> servicios = repositorioServicio.findByEmpresaAndActivoTrueWithEspecialidades(dueno.getEmpresa());
        
        // Obtener overrides del profesional
        List<ProfesionalServicio> overrides = repositorioProfesionalServicio.findByProfesional(profesional);
        
        List<ProfesionalServicioResponse> resultado = new ArrayList<>();
        
        for (Servicio servicio : servicios) {
            // Verificar si el servicio comparte al menos una especialidad con el profesional
            boolean compatiblePorEspecialidad = servicio.getEspecialidades().stream()
                    .anyMatch(especialidadServicio -> 
                        profesional.getEspecialidades().stream()
                            .anyMatch(especialidadProf -> 
                                especialidadProf.getId().equals(especialidadServicio.getId())
                            )
                    );
            
            if (compatiblePorEspecialidad) {
                // Buscar si hay override
                Optional<ProfesionalServicio> override = overrides.stream()
                        .filter(ps -> ps.getServicio().getId().equals(servicio.getId()))
                        .findFirst();
                
                ProfesionalServicioResponse response = new ProfesionalServicioResponse();
                response.setServicioId(servicio.getId());
                response.setNombre(servicio.getNombre());
                response.setDescripcion(servicio.getDescripcion());
                response.setDuracionMinutos(servicio.getDuracionMinutos());
                response.setPrecio(servicio.getPrecio().doubleValue());
                
                if (override.isPresent()) {
                    // Hay override explícito
                    response.setDisponible(override.get().getActivo());
                    response.setHeredado(false);
                } else {
                    // Disponible por herencia (especialidad)
                    response.setDisponible(true);
                    response.setHeredado(true);
                }
                
                resultado.add(response);
            }
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
        
        // Verificar que el servicio es compatible con las especialidades del profesional
        boolean compatible = servicio.getEspecialidades().stream()
                .anyMatch(especialidadServicio -> 
                    profesional.getEspecialidades().stream()
                        .anyMatch(especialidadProf -> 
                            especialidadProf.getId().equals(especialidadServicio.getId())
                        )
                );
        
        if (!compatible) {
            throw new IllegalArgumentException(
                "El servicio no es compatible con las especialidades del profesional"
            );
        }
        
        // Buscar o crear override
        Optional<ProfesionalServicio> existente = repositorioProfesionalServicio
                .findByProfesionalAndServicio(profesional, servicio);
        
        if (disponible) {
            // Si se activa y hay override, eliminarlo (volver a herencia)
            existente.ifPresent(repositorioProfesionalServicio::delete);
        } else {
            // Si se desactiva, crear/actualizar override
            ProfesionalServicio ps = existente.orElse(new ProfesionalServicio());
            ps.setProfesional(profesional);
            ps.setServicio(servicio);
            ps.setActivo(false);
            repositorioProfesionalServicio.save(ps);
        }
    }
    
    /**
     * Limpia overrides de ProfesionalServicio que ya no son compatibles
     * con las especialidades actuales del profesional (HERENCIA-002)
     */
    /**
     * MEJORA-003: Valida que todas las especialidades estén activas.
     * 
     * @param especialidades Especialidades a validar
     * @throws IllegalArgumentException si alguna especialidad está desactivada
     */
    private void validarEspecialidadesActivas(Set<Especialidad> especialidades) {
        List<String> especialidadesInactivas = especialidades.stream()
                .filter(e -> !e.getActiva())
                .map(Especialidad::getNombre)
                .collect(Collectors.toList());
        
        if (!especialidadesInactivas.isEmpty()) {
            throw new IllegalArgumentException(
                "Las siguientes especialidades están desactivadas: " + 
                String.join(", ", especialidadesInactivas)
            );
        }
    }

    private void limpiarOverridesHuerfanos(Profesional profesional) {
        List<ProfesionalServicio> overrides = repositorioProfesionalServicio.findByProfesional(profesional);
        
        List<ProfesionalServicio> huerfanos = overrides.stream()
            .filter(override -> {
                // Verificar si el servicio sigue siendo compatible
                boolean sigueCompatible = override.getServicio().getEspecialidades().stream()
                    .anyMatch(especialidadServicio -> 
                        profesional.getEspecialidades().stream()
                            .anyMatch(especialidadProf -> 
                                especialidadProf.getId().equals(especialidadServicio.getId())
                            )
                    );
                return !sigueCompatible; // Retornar los que YA NO son compatibles
            })
            .collect(Collectors.toList());
        
        // Eliminar overrides huérfanos
        if (!huerfanos.isEmpty()) {
            repositorioProfesionalServicio.deleteAll(huerfanos);
        }
    }
}

