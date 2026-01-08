package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.ProfesionalResponse;
import com.example.sitema_de_turnos.dto.RegistroProfesionalRequest;
import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;
import com.example.sitema_de_turnos.excepcion.CuentaDesactivadaException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.modelo.Dueno;
import com.example.sitema_de_turnos.modelo.Especialidad;
import com.example.sitema_de_turnos.modelo.Profesional;
import com.example.sitema_de_turnos.modelo.RolUsuario;
import com.example.sitema_de_turnos.repositorio.RepositorioDueno;
import com.example.sitema_de_turnos.repositorio.RepositorioEspecialidad;
import com.example.sitema_de_turnos.repositorio.RepositorioProfesional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioProfesional {

    private final RepositorioProfesional repositorioProfesional;
    private final RepositorioDueno repositorioDueno;
    private final RepositorioEspecialidad repositorioEspecialidad;
    private final PasswordEncoder passwordEncoder;

    /**
     * Valida que el dueño exista, esté activo y tenga una empresa activa
     */
    private Dueno validarYObtenerDueno(String emailDueno) {
        Dueno dueno = repositorioDueno.findByEmail(emailDueno)
                .orElseThrow(() -> new RecursoNoEncontradoException("Dueño no encontrado"));
        
        if (!dueno.getActivo()) {
            throw new CuentaDesactivadaException("Su cuenta ha sido desactivada. Contacte al administrador.");
        }
        
        if (dueno.getEmpresa() == null) {
            throw new CuentaDesactivadaException("No tiene una empresa asignada");
        }
        
        if (!dueno.getEmpresa().getActiva()) {
            throw new CuentaDesactivadaException("Su empresa ha sido desactivada. Contacte al administrador.");
        }
        
        return dueno;
    }

    @Transactional(readOnly = true)
    public List<ProfesionalResponse> obtenerProfesionalesPorEmpresa(String emailDueno) {
        Dueno dueno = validarYObtenerDueno(emailDueno);
        
        List<Profesional> profesionales = repositorioProfesional.findByEmpresa(dueno.getEmpresa());
        return profesionales.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProfesionalResponse crearProfesional(String emailDueno, RegistroProfesionalRequest dto) {
        // Validar dueño activo y empresa activa
        Dueno dueno = validarYObtenerDueno(emailDueno);

        // Validar email único
        if (repositorioProfesional.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Los datos proporcionados no son válidos o ya están registrados");
        }

        // Obtener o crear especialidades
        Set<Especialidad> especialidades = obtenerOCrearEspecialidades(dto.getEspecialidades());

        // Crear profesional
        Profesional profesional = new Profesional();
        profesional.setNombre(dto.getNombre());
        profesional.setApellido(dto.getApellido());
        profesional.setEmail(dto.getEmail());
        profesional.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        profesional.setTelefono(dto.getTelefono());
        profesional.setEspecialidades(especialidades);
        profesional.setDescripcion(dto.getDescripcion());
        profesional.setRol(RolUsuario.PROFESIONAL);
        profesional.setActivo(true);
        profesional.setEmpresa(dueno.getEmpresa()); // Usa la empresa del dueño autenticado
        profesional.setFechaCreacion(LocalDateTime.now());
        profesional.setFechaActualizacion(LocalDateTime.now());

        Profesional guardado = repositorioProfesional.save(profesional);
        return convertirAResponse(guardado);
    }

    @Transactional
    public ProfesionalResponse actualizarProfesional(String emailDueno, Long idProfesional, RegistroProfesionalRequest dto) {
        // Validar dueño activo y empresa activa
        Dueno dueno = validarYObtenerDueno(emailDueno);

        // Buscar profesional
        Profesional profesional = repositorioProfesional.findById(idProfesional)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        // Validar que el profesional pertenece a la empresa del dueño
        if (!profesional.getEmpresa().getId().equals(dueno.getEmpresa().getId())) {
            throw new AccesoDenegadoException("No tiene permiso para modificar este profesional");
        }

        // Validar email único (si cambió)
        if (!profesional.getEmail().equals(dto.getEmail())) {
            if (repositorioProfesional.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("Los datos proporcionados no son válidos o ya están registrados");
            }
            profesional.setEmail(dto.getEmail());
        }

        // Obtener o crear especialidades
        Set<Especialidad> especialidades = obtenerOCrearEspecialidades(dto.getEspecialidades());

        // Actualizar datos
        profesional.setNombre(dto.getNombre());
        profesional.setApellido(dto.getApellido());
        profesional.setTelefono(dto.getTelefono());
        profesional.setEspecialidades(especialidades);
        profesional.setDescripcion(dto.getDescripcion());
        
        // Solo actualizar contraseña si se proporciona una nueva
        if (dto.getContrasena() != null && !dto.getContrasena().isEmpty()) {
            profesional.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        }
        
        profesional.setFechaActualizacion(LocalDateTime.now());

        Profesional actualizado = repositorioProfesional.save(profesional);
        return convertirAResponse(actualizado);
    }

    @Transactional
    public void eliminarProfesional(String emailDueno, Long idProfesional) {
        // Validar dueño activo y empresa activa
        Dueno dueno = validarYObtenerDueno(emailDueno);

        // Buscar profesional
        Profesional profesional = repositorioProfesional.findById(idProfesional)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        // Validar que el profesional pertenece a la empresa del dueño
        if (!profesional.getEmpresa().getId().equals(dueno.getEmpresa().getId())) {
            throw new AccesoDenegadoException("No tiene permiso para desactivar este profesional");
        }

        // Desactivar en lugar de eliminar (soft delete)
        profesional.setActivo(false);
        profesional.setFechaActualizacion(LocalDateTime.now());
        repositorioProfesional.save(profesional);
    }

    private Set<Especialidad> obtenerOCrearEspecialidades(List<String> nombresEspecialidades) {
        Set<Especialidad> especialidades = new HashSet<>();
        
        for (String nombre : nombresEspecialidades) {
            Especialidad especialidad = repositorioEspecialidad.findByNombre(nombre)
                    .orElseGet(() -> {
                        Especialidad nueva = new Especialidad();
                        nueva.setNombre(nombre);
                        nueva.setActiva(true);
                        nueva.setFechaCreacion(LocalDateTime.now());
                        nueva.setFechaActualizacion(LocalDateTime.now());
                        return repositorioEspecialidad.save(nueva);
                    });
            especialidades.add(especialidad);
        }
        
        return especialidades;
    }

    private ProfesionalResponse convertirAResponse(Profesional profesional) {
        ProfesionalResponse response = new ProfesionalResponse();
        response.setId(profesional.getId());
        response.setNombre(profesional.getNombre());
        response.setApellido(profesional.getApellido());
        response.setEmail(profesional.getEmail());
        response.setTelefono(profesional.getTelefono());
        response.setEspecialidades(
            profesional.getEspecialidades().stream()
                .map(Especialidad::getNombre)
                .collect(Collectors.toList())
        );
        response.setDescripcion(profesional.getDescripcion());
        response.setEmpresaId(profesional.getEmpresa().getId());
        response.setEmpresaNombre(profesional.getEmpresa().getNombre());
        return response;
    }
}
