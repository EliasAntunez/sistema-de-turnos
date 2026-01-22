package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.RegistroServicioRequest;
import com.example.sitema_de_turnos.dto.ServicioResponse;
import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.mapper.ServicioMapper;
import com.example.sitema_de_turnos.modelo.*;
import com.example.sitema_de_turnos.repositorio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("removal")
public class ServicioServicio {

    private final RepositorioServicio repositorioServicio;
    private final RepositorioEspecialidad repositorioEspecialidad;
    private final RepositorioProfesionalServicio repositorioProfesionalServicio;
    private final ServicioValidacionDueno servicioValidacionDueno;

    @Transactional
    public ServicioResponse crearServicio(String emailDueno, RegistroServicioRequest request) {
        // Validar duración y precio
        if (request.getDuracionMinutos() <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor a 0 minutos");
        }
        if (request.getPrecio().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }

        // RIESGO-003: Validar dueño y empresa activa
        Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);

        // Validar especialidades
        Set<Especialidad> especialidades = request.getEspecialidades().stream()
                .map(nombre -> repositorioEspecialidad.findByNombreIgnoreCase(nombre)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada: " + nombre)))
                .collect(Collectors.toSet());

        if (especialidades.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una especialidad");
        }
        
        // MEJORA-003: Validar que todas las especialidades estén activas
        validarEspecialidadesActivas(especialidades);

        // Crear servicio
        com.example.sitema_de_turnos.modelo.Servicio servicio = new com.example.sitema_de_turnos.modelo.Servicio();
        servicio.setNombre(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(request.getNombre()));
        servicio.setDescripcion(request.getDescripcion());
        servicio.setDuracionMinutos(request.getDuracionMinutos());
        servicio.setBufferMinutos(request.getBufferMinutos());
        servicio.setPrecio(request.getPrecio());
        servicio.setEmpresa(dueno.getEmpresa());
        servicio.setEspecialidades(especialidades);
        servicio.setActivo(true);
        servicio.setFechaCreacion(LocalDateTime.now());

        servicio = repositorioServicio.save(servicio);

        return ServicioMapper.toResponse(servicio);
    }

    @Transactional
    public ServicioResponse actualizarServicio(String emailDueno, Long servicioId, RegistroServicioRequest request) {
        // Validar duración y precio
        if (request.getDuracionMinutos() <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor a 0 minutos");
        }
        if (request.getPrecio().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }

        // RIESGO-003: Validar dueño y empresa activa
        Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);

        // Obtener servicio y validar pertenencia
        com.example.sitema_de_turnos.modelo.Servicio servicio = repositorioServicio.findById(servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado"));

        if (!servicio.getEmpresa().getId().equals(dueno.getEmpresa().getId())) {
            throw new AccesoDenegadoException("No puede modificar servicios de otra empresa");
        }

        // Validar especialidades
        Set<Especialidad> especialidades = request.getEspecialidades().stream()
                .map(nombre -> repositorioEspecialidad.findByNombreIgnoreCase(nombre)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada: " + nombre)))
                .collect(Collectors.toSet());

        if (especialidades.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una especialidad");
        }
        
        // MEJORA-003: Validar que todas las especialidades estén activas
        validarEspecialidadesActivas(especialidades);

        // Guardar especialidades anteriores para detectar cambios
        Set<Especialidad> especialidadesAnteriores = new HashSet<>(servicio.getEspecialidades());

        // Actualizar servicio
        servicio.setNombre(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(request.getNombre()));
        servicio.setDescripcion(request.getDescripcion());
        servicio.setDuracionMinutos(request.getDuracionMinutos());
        servicio.setBufferMinutos(request.getBufferMinutos());
        servicio.setPrecio(request.getPrecio());
        servicio.setEspecialidades(especialidades);

        servicio = repositorioServicio.save(servicio);

        // HERENCIA-003: Si cambiaron las especialidades, limpiar overrides huérfanos
        if (!especialidadesAnteriores.equals(especialidades)) {
            limpiarOverridesHuerfanosDeServicio(servicio);
        }

        return ServicioMapper.toResponse(servicio);
    }

    @Transactional(readOnly = true)
    public List<ServicioResponse> obtenerServiciosPorEmpresa(String emailDueno) {
        // RIESGO-003: Validar dueño y empresa activa
        Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);

        List<com.example.sitema_de_turnos.modelo.Servicio> servicios = 
                repositorioServicio.findByEmpresa(dueno.getEmpresa());

        return servicios.stream()
                .map(ServicioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void activarServicio(String emailDueno, Long servicioId) {
        cambiarEstadoServicio(emailDueno, servicioId, true);
    }

    @Transactional
    public void desactivarServicio(String emailDueno, Long servicioId) {
        cambiarEstadoServicio(emailDueno, servicioId, false);
    }

    private void cambiarEstadoServicio(String emailDueno, Long servicioId, boolean activo) {
        // RIESGO-003: Validar dueño y empresa activa
        Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);

        // Obtener servicio y validar pertenencia
        com.example.sitema_de_turnos.modelo.Servicio servicio = repositorioServicio.findById(servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado"));

        if (!servicio.getEmpresa().getId().equals(dueno.getEmpresa().getId())) {
            throw new AccesoDenegadoException("No puede modificar servicios de otra empresa");
        }

        servicio.setActivo(activo);
        repositorioServicio.save(servicio);
    }

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

    /**
     * HERENCIA-003: Limpia los overrides (ProfesionalServicio) que se volvieron huérfanos
     * al cambiar las especialidades de un servicio.
     * 
     * Un override es huérfano cuando el profesional ya no tiene ninguna especialidad
     * en común con el servicio.
     */
    private void limpiarOverridesHuerfanosDeServicio(com.example.sitema_de_turnos.modelo.Servicio servicio) {
        // Obtener todos los overrides para este servicio
        List<ProfesionalServicio> overrides = repositorioProfesionalServicio.findByServicio(servicio);
        
        // Filtrar los que se volvieron incompatibles (sin especialidades en común)
        List<ProfesionalServicio> huerfanos = overrides.stream()
                .filter(override -> {
                    Profesional profesional = override.getProfesional();
                    Set<Especialidad> especialidadesProfesional = profesional.getEspecialidades();
                    Set<Especialidad> especialidadesServicio = servicio.getEspecialidades();
                    
                    // Verificar si hay al menos una especialidad en común
                    boolean tieneEspecialidadEnComun = especialidadesProfesional.stream()
                            .anyMatch(especialidadesServicio::contains);
                    
                    // Es huérfano si NO tiene especialidades en común
                    return !tieneEspecialidadEnComun;
                })
                .collect(Collectors.toList());
        
        // Eliminar overrides huérfanos
        if (!huerfanos.isEmpty()) {
            repositorioProfesionalServicio.deleteAll(huerfanos);
        }
    }
}
