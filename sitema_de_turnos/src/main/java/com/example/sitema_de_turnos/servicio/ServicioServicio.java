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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioServicio {

    private final RepositorioServicio repositorioServicio;
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

        // Crear servicio
        com.example.sitema_de_turnos.modelo.Servicio servicio = new com.example.sitema_de_turnos.modelo.Servicio();
        servicio.setNombre(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(request.getNombre()));
        servicio.setDescripcion(request.getDescripcion());
        servicio.setDuracionMinutos(request.getDuracionMinutos());
        // Si el DTO no trae buffer, usar el buffer por defecto de la empresa (fallback: 10 min).
        int bufferCreacion = (request.getBufferMinutos() != null)
            ? request.getBufferMinutos()
            : (dueno.getEmpresa().getBufferPorDefecto() != null
                ? dueno.getEmpresa().getBufferPorDefecto()
                : 10);
        servicio.setBufferMinutos(bufferCreacion);
        servicio.setPrecio(request.getPrecio());
        servicio.setEmpresa(dueno.getEmpresa());
        servicio.setActivo(true);
        servicio.setFechaCreacion(LocalDateTime.now());

        servicio = repositorioServicio.save(servicio);

        // Auto-onboarding para empresas unipersonales (solo cuenta profesionales activos)
        List<Profesional> profesionalesActivos = dueno.getEmpresa().getProfesionales()
                .stream()
                .filter(Profesional::getActivo)
                .collect(java.util.stream.Collectors.toList());
        if (profesionalesActivos.size() == 1) {
            ProfesionalServicio ps = new ProfesionalServicio();
            ps.setProfesional(profesionalesActivos.get(0));
            ps.setServicio(servicio);
            ps.setActivo(true);
            repositorioProfesionalServicio.save(ps);
        }

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

        // Actualizar servicio
        // Nota: cambios de duración/precio sólo afectan a nuevas reservas.
        // Los turnos ya agendados conservan su duracionMinutos original (snapshot en la entidad Turno).
        servicio.setNombre(com.example.sitema_de_turnos.util.NormalizadorDatos.normalizarNombre(request.getNombre()));
        servicio.setDescripcion(request.getDescripcion());
        servicio.setDuracionMinutos(request.getDuracionMinutos());
        // Si el DTO no trae buffer, conservar el valor ya persistido (no pisar con null).
        if (request.getBufferMinutos() != null) {
            servicio.setBufferMinutos(request.getBufferMinutos());
        }
        servicio.setPrecio(request.getPrecio());

        servicio = repositorioServicio.save(servicio);

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

        // La desactivación es siempre libre: el servicio deja de aparecer para nuevas
        // reservas, pero todos los turnos existentes conservan su snapshot de datos.
        servicio.setActivo(activo);
        repositorioServicio.save(servicio);
    }
}
