package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.publico.*;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.excepcion.ValidacionException;
import com.example.sitema_de_turnos.modelo.*;
import com.example.sitema_de_turnos.repositorio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioPublico {

    private final RepositorioEmpresa repositorioEmpresa;
    private final RepositorioServicio repositorioServicio;
    private final RepositorioProfesional repositorioProfesional;
    private final RepositorioDisponibilidadProfesional repositorioDisponibilidad;
    private final RepositorioHorarioEmpresa repositorioHorarioEmpresa;
    private final RepositorioBloqueoFecha repositorioBloqueoFecha;
    private final RepositorioTurno repositorioTurno;

    /**
     * Obtener información pública de una empresa por slug
     */
    @Transactional(readOnly = true)
    public EmpresaPublicaResponse obtenerEmpresaPorSlug(String slug) {
        Empresa empresa = repositorioEmpresa.findBySlugAndActivaTrue(slug)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));

        return new EmpresaPublicaResponse(
                empresa.getId(),
                empresa.getNombre(),
                empresa.getSlug(),
                empresa.getDescripcion(),
                empresa.getDireccion(),
                empresa.getCiudad(),
                empresa.getProvincia(),
                empresa.getTelefono(),
                empresa.getEmail(),
                empresa.getDiasMaximosReserva()
        );
    }

    /**
     * Obtener servicios activos de una empresa
     */
    @Transactional(readOnly = true)
    public List<ServicioPublicoResponse> obtenerServiciosPorEmpresa(String slug) {
        Empresa empresa = repositorioEmpresa.findBySlugAndActivaTrue(slug)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));

        List<Servicio> servicios = repositorioServicio.findByEmpresaAndActivoTrue(empresa);

        return servicios.stream()
                .map(s -> new ServicioPublicoResponse(
                        s.getId(),
                        s.getNombre(),
                        s.getDescripcion(),
                        s.getDuracionMinutos(),
                        s.getPrecio()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Obtener profesionales que pueden dar un servicio específico.
     * Considera: especialidades + servicios NO bloqueados
     */
    @Transactional(readOnly = true)
    public List<ProfesionalPublicoResponse> obtenerProfesionalesPorServicio(String slug, Long servicioId) {
        Empresa empresa = repositorioEmpresa.findBySlugAndActivaTrue(slug)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));

        Servicio servicio = repositorioServicio.findById(servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado"));

        if (!servicio.getEmpresa().getId().equals(empresa.getId())) {
            throw new ValidacionException("El servicio no pertenece a esta empresa");
        }

        // Obtener todos los profesionales de la empresa con especialidades
        List<Profesional> profesionales = repositorioProfesional.findByEmpresaWithEspecialidades(empresa);

        // Filtrar: deben tener al menos una especialidad en común con el servicio
        // Y el servicio NO debe estar en serviciosBloqueados del profesional
        return profesionales.stream()
                .filter(p -> p.getActivo()) // Solo profesionales activos
                .filter(p -> tieneEspecialidadCompatible(p, servicio))
                .filter(p -> !p.getServiciosBloqueados().contains(servicio))
                .map(p -> new ProfesionalPublicoResponse(
                        p.getId(),
                        p.getNombre(),
                        p.getApellido(),
                        p.getDescripcion()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Verificar si el profesional tiene al menos una especialidad en común con el servicio
     */
    private boolean tieneEspecialidadCompatible(Profesional profesional, Servicio servicio) {
        return profesional.getEspecialidades().stream()
                .anyMatch(e -> servicio.getEspecialidades().contains(e));
    }

    /**
     * Obtener slots disponibles para un servicio, profesional y fecha específica.
     * Considera: disponibilidad/horarios empresa + bloqueos + duración servicio + buffer
     */
    @Transactional(readOnly = true)
    public List<SlotDisponibleResponse> obtenerSlotsDisponibles(
            String empresaSlug,
            Long servicioId,
            Long profesionalId,
            LocalDate fecha
    ) {
        // Validar empresa
        Empresa empresa = repositorioEmpresa.findBySlugAndActivaTrue(empresaSlug)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));

        // Validar que la fecha esté dentro del rango permitido
        LocalDate hoy = LocalDate.now();
        LocalDate fechaMaxima = hoy.plusDays(empresa.getDiasMaximosReserva());
        if (fecha.isBefore(hoy)) {
            throw new ValidacionException("No se pueden reservar turnos en fechas pasadas");
        }
        if (fecha.isAfter(fechaMaxima)) {
            throw new ValidacionException("La fecha excede el límite de reserva permitido");
        }

        // Validar servicio
        Servicio servicio = repositorioServicio.findById(servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado"));

        if (!servicio.getEmpresa().getId().equals(empresa.getId())) {
            throw new ValidacionException("El servicio no pertenece a esta empresa");
        }

        // Validar profesional
        Profesional profesional = repositorioProfesional.findByIdWithEspecialidades(profesionalId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        if (!profesional.getEmpresa().getId().equals(empresa.getId())) {
            throw new ValidacionException("El profesional no pertenece a esta empresa");
        }

        if (!profesional.getActivo()) {
            throw new ValidacionException("El profesional no está activo");
        }

        // Validar que el profesional pueda dar este servicio
        if (!tieneEspecialidadCompatible(profesional, servicio)) {
            throw new ValidacionException("El profesional no tiene la especialidad requerida para este servicio");
        }

        if (profesional.getServiciosBloqueados().contains(servicio)) {
            throw new ValidacionException("El profesional tiene bloqueado este servicio");
        }

        // Verificar si el profesional tiene bloqueo en esta fecha
        if (tieneBloqueoDeFecha(profesional, fecha)) {
            return new ArrayList<>(); // No hay slots disponibles
        }

        // Obtener día de la semana
        DiaSemana diaSemana = convertirDayOfWeekADiaSemana(fecha.getDayOfWeek());

        // Obtener horarios del profesional (con fallback a empresa)
        List<RangoHorario> rangosHorarios = obtenerRangosHorarios(profesional, diaSemana);

        if (rangosHorarios.isEmpty()) {
            return new ArrayList<>(); // No hay horarios configurados
        }

        // Calcular buffer efectivo
        Integer bufferMinutos = calcularBufferEfectivo(servicio, profesional, empresa);

        // Generar slots
        return generarSlots(rangosHorarios, fecha, servicio.getDuracionMinutos(), bufferMinutos, profesional);
    }

    /**
     * Verificar si el profesional tiene bloqueo en una fecha específica
     */
    private boolean tieneBloqueoDeFecha(Profesional profesional, LocalDate fecha) {
        List<BloqueoFecha> bloqueos = repositorioBloqueoFecha.findByProfesionalAndActivoTrue(profesional);
        
        return bloqueos.stream()
                .anyMatch(bloqueo -> {
                    LocalDate inicio = bloqueo.getFechaInicio();
                    LocalDate fin = bloqueo.getFechaFin() != null ? bloqueo.getFechaFin() : inicio;
                    return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
                });
    }

    /**
     * Obtener rangos horarios del profesional (con fallback a empresa)
     */
    private List<RangoHorario> obtenerRangosHorarios(Profesional profesional, DiaSemana diaSemana) {
        // Primero intentar obtener disponibilidad propia del profesional
        List<DisponibilidadProfesional> disponibilidades = 
                repositorioDisponibilidad.findByProfesionalAndDiaSemanaAndActivoTrue(profesional, diaSemana);

        if (!disponibilidades.isEmpty()) {
            return disponibilidades.stream()
                    .map(d -> new RangoHorario(d.getHoraInicio(), d.getHoraFin()))
                    .collect(Collectors.toList());
        }

        // Fallback: usar horarios de la empresa
        List<HorarioEmpresa> horariosEmpresa = 
                repositorioHorarioEmpresa.findByEmpresaAndDiaSemanaAndActivoTrue(profesional.getEmpresa(), diaSemana);

        return horariosEmpresa.stream()
                .map(h -> new RangoHorario(h.getHoraInicio(), h.getHoraFin()))
                .collect(Collectors.toList());
    }

    /**
     * Calcular buffer efectivo: Servicio > Empresa
     */
    private Integer calcularBufferEfectivo(Servicio servicio, Profesional profesional, Empresa empresa) {
        if (servicio.getBufferMinutos() != null) {
            return servicio.getBufferMinutos();
        }
        return empresa.getBufferPorDefecto() != null ? empresa.getBufferPorDefecto() : 10;
    }

    /**
     * Generar slots disponibles en puntos significativos del calendario.
     * Estrategia: generar slots SOLO en:
     * 1. Inicio de cada hueco libre
     * 2. Fin exacto de cada turno existente (si el servicio cabe después)
     * 
     * Esto maximiza ocupación sin granularidad artificial.
     */
    private List<SlotDisponibleResponse> generarSlots(
            List<RangoHorario> rangos,
            LocalDate fecha,
            Integer duracionServicio,
            Integer buffer,
            Profesional profesional
    ) {
        List<SlotDisponibleResponse> slots = new ArrayList<>();
        Integer duracionTotal = duracionServicio + buffer;

        // Obtener turnos activos ordenados por hora de inicio
        List<Turno> turnosExistentes = repositorioTurno.findTurnosActivosByProfesionalAndFecha(
                profesional, fecha);
        
        turnosExistentes.sort(Comparator.comparing(Turno::getHoraInicio));

        for (RangoHorario rango : rangos) {
            // Filtrar turnos que están dentro de este rango horario
            List<Turno> turnosEnRango = turnosExistentes.stream()
                    .filter(t -> !t.getHoraInicio().isBefore(rango.horaInicio) && 
                                 t.getHoraInicio().isBefore(rango.horaFin))
                    .collect(Collectors.toList());

            if (turnosEnRango.isEmpty()) {
                // Sin turnos: todo el rango es un hueco libre
                // Generar múltiples slots dentro del hueco
                generarSlotsEnHueco(slots, fecha, rango.horaInicio, rango.horaFin, 
                                   duracionServicio, duracionTotal, profesional);
            } else {
                // Con turnos: identificar huecos libres
                LocalTime inicioBusqueda = rango.horaInicio;

                for (Turno turno : turnosEnRango) {
                    // Hueco ANTES del turno actual
                    if (inicioBusqueda.isBefore(turno.getHoraInicio())) {
                        // Generar múltiples slots en este hueco
                        generarSlotsEnHueco(slots, fecha, inicioBusqueda, turno.getHoraInicio(), 
                                           duracionServicio, duracionTotal, profesional);
                    }

                    // Preparar para buscar después de este turno
                    // El siguiente hueco comienza exactamente cuando termina este turno
                    inicioBusqueda = turno.getHoraFin();
                }

                // Hueco DESPUÉS del último turno hasta el fin del rango
                if (inicioBusqueda.isBefore(rango.horaFin)) {
                    // Generar múltiples slots en este último hueco
                    generarSlotsEnHueco(slots, fecha, inicioBusqueda, rango.horaFin, 
                                       duracionServicio, duracionTotal, profesional);
                }
            }
        }

        return slots;
    }

    /**
     * Generar múltiples slots dentro de un hueco libre.
     * Avanza por (duración + buffer) para evitar solapamientos.
     */
    private void generarSlotsEnHueco(List<SlotDisponibleResponse> slots, LocalDate fecha,
                                     LocalTime inicioHueco, LocalTime finHueco,
                                     Integer duracionServicio, Integer duracionTotal,
                                     Profesional profesional) {
        LocalTime horaActual = inicioHueco;
        
        while (horaActual.plusMinutes(duracionTotal).compareTo(finHueco) <= 0) {
            slots.add(crearSlot(fecha, horaActual, duracionServicio, profesional));
            horaActual = horaActual.plusMinutes(duracionTotal);
        }
    }

    /**
     * Crear un slot disponible
     */
    private SlotDisponibleResponse crearSlot(LocalDate fecha, LocalTime horaInicio, 
                                             Integer duracionServicio, Profesional profesional) {
        LocalDateTime inicio = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fin = inicio.plusMinutes(duracionServicio);
        
        return new SlotDisponibleResponse(
                inicio,
                fin,
                profesional.getId(),
                profesional.getNombre() + " " + profesional.getApellido()
        );
    }

    /**
     * Convertir java.time.DayOfWeek a DiaSemana
     */
    private DiaSemana convertirDayOfWeekADiaSemana(java.time.DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return DiaSemana.LUNES;
            case TUESDAY: return DiaSemana.MARTES;
            case WEDNESDAY: return DiaSemana.MIERCOLES;
            case THURSDAY: return DiaSemana.JUEVES;
            case FRIDAY: return DiaSemana.VIERNES;
            case SATURDAY: return DiaSemana.SABADO;
            case SUNDAY: return DiaSemana.DOMINGO;
            default: throw new IllegalArgumentException("Día de la semana no válido: " + dayOfWeek);
        }
    }

    /**
     * Clase auxiliar para rangos horarios
     */
    private static class RangoHorario {
        LocalTime horaInicio;
        LocalTime horaFin;

        RangoHorario(LocalTime horaInicio, LocalTime horaFin) {
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
        }
    }
}
