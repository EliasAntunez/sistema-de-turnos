package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.EspecialidadResponse;
import com.example.sitema_de_turnos.dto.RegistroEspecialidadRequest;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.modelo.Especialidad;
import com.example.sitema_de_turnos.repositorio.RepositorioEspecialidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioEspecialidad {

    private final RepositorioEspecialidad repositorioEspecialidad;

    @Transactional
    public EspecialidadResponse crearEspecialidad(RegistroEspecialidadRequest request) {
        // Validar que no exista una especialidad con el mismo nombre (case-insensitive)
        if (repositorioEspecialidad.findByNombreIgnoreCase(request.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una especialidad con el nombre: " + request.getNombre());
        }

        Especialidad especialidad = new Especialidad();
        especialidad.setNombre(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(request.getNombre()));
        especialidad.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        especialidad.setActiva(true);
        especialidad.setFechaCreacion(LocalDateTime.now());
        especialidad.setFechaActualizacion(LocalDateTime.now());

        especialidad = repositorioEspecialidad.save(especialidad);

        return convertirAResponse(especialidad);
    }

    @Transactional
    public EspecialidadResponse actualizarEspecialidad(Long id, RegistroEspecialidadRequest request) {
        Especialidad especialidad = repositorioEspecialidad.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada"));

        // Validar que no exista otra especialidad con el mismo nombre (case-insensitive)
        repositorioEspecialidad.findByNombreIgnoreCase(request.getNombre())
                .ifPresent(existente -> {
                    if (!existente.getId().equals(id)) {
                        throw new IllegalArgumentException("Ya existe una especialidad con el nombre: " + request.getNombre());
                    }
                });

        especialidad.setNombre(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(request.getNombre()));
        especialidad.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        especialidad.setFechaActualizacion(LocalDateTime.now());

        especialidad = repositorioEspecialidad.save(especialidad);

        return convertirAResponse(especialidad);
    }

    @Transactional(readOnly = true)
    public List<EspecialidadResponse> listarTodasLasEspecialidades() {
        return repositorioEspecialidad.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EspecialidadResponse> listarEspecialidadesActivas() {
        return repositorioEspecialidad.findAll().stream()
                .filter(Especialidad::getActiva)
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EspecialidadResponse obtenerEspecialidadPorId(Long id) {
        Especialidad especialidad = repositorioEspecialidad.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada"));
        return convertirAResponse(especialidad);
    }

    @Transactional
    public void activarEspecialidad(Long id) {
        Especialidad especialidad = repositorioEspecialidad.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada"));
        
        especialidad.setActiva(true);
        especialidad.setFechaActualizacion(LocalDateTime.now());
        repositorioEspecialidad.save(especialidad);
    }

    @Transactional
    public void desactivarEspecialidad(Long id) {
        Especialidad especialidad = repositorioEspecialidad.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada"));
        
        especialidad.setActiva(false);
        especialidad.setFechaActualizacion(LocalDateTime.now());
        repositorioEspecialidad.save(especialidad);
    }

    private EspecialidadResponse convertirAResponse(Especialidad especialidad) {
        EspecialidadResponse response = new EspecialidadResponse();
        response.setId(especialidad.getId());
        response.setNombre(especialidad.getNombre());
        response.setDescripcion(especialidad.getDescripcion());
        response.setActiva(especialidad.getActiva());
        response.setFechaCreacion(especialidad.getFechaCreacion());
        return response;
    }
}
