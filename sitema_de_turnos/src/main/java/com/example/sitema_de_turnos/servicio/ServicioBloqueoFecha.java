package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.*;
import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.excepcion.ValidacionException;
import com.example.sitema_de_turnos.modelo.*;
import com.example.sitema_de_turnos.repositorio.RepositorioBloqueoFecha;
import com.example.sitema_de_turnos.repositorio.RepositorioTurno;
import com.example.sitema_de_turnos.repositorio.RepositorioDisponibilidadProfesional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * CORREGIDO: Ahora usa ServicioValidacionProfesional para validar empresa activa
 */
@Service
@RequiredArgsConstructor
public class ServicioBloqueoFecha {

    private final RepositorioBloqueoFecha repositorioBloqueoFecha;
    private final RepositorioTurno repositorioTurno;
    private final RepositorioDisponibilidadProfesional repositorioDisponibilidadProfesional;
    private final ServicioValidacionProfesional servicioValidacionProfesional;

    @Transactional
    public BloqueoFechaResponse crearBloqueo(String emailProfesional, BloqueoFechaRequest request) {
        // Validar fechas
        if (request.getFechaFin() != null && request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior o igual a la fecha de inicio");
        }

        // Obtener profesional
        Profesional profesional = obtenerProfesional(emailProfesional);

        // Verificar solapamientos con bloqueos existentes
        List<BloqueoFecha> bloqueosExistentes;
        if (request.getFechaFin() != null) {
            bloqueosExistentes = repositorioBloqueoFecha.findBloqueosEnRango(
                    profesional, request.getFechaInicio(), request.getFechaFin());
        } else {
            bloqueosExistentes = repositorioBloqueoFecha.findBloqueoEnFecha(
                    profesional, request.getFechaInicio());
        }

        if (!bloqueosExistentes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ya existe un bloqueo que se solapa con las fechas indicadas");
        }

        // NUEVA VALIDACIÓN: Verificar conflictos con turnos
        ConflictosBloqueoResponse conflictos = verificarConflictosConTurnos(profesional, request);
        if (conflictos.isTieneConflictos()) {
            throw new ValidacionException("Existen turnos en conflicto. Use el endpoint de resolución de conflictos.");
        }

        // Crear bloqueo
        BloqueoFecha bloqueo = new BloqueoFecha();
        bloqueo.setProfesional(profesional);
        bloqueo.setFechaInicio(request.getFechaInicio());
        bloqueo.setFechaFin(request.getFechaFin());
        bloqueo.setMotivo(request.getMotivo());
        bloqueo.setActivo(true);

        bloqueo = repositorioBloqueoFecha.save(bloqueo);

        return convertirAResponse(bloqueo);
    }

    /**
     * Verificar si existen turnos en conflicto con el bloqueo
     */
    @Transactional(readOnly = true)
    public ConflictosBloqueoResponse verificarConflictosConTurnos(Profesional profesional, BloqueoFechaRequest request) {
        LocalDate fechaInicio = request.getFechaInicio();
        LocalDate fechaFin = request.getFechaFin() != null ? request.getFechaFin() : request.getFechaInicio();

        // Buscar turnos activos en el rango
        List<Turno> turnosEnRango = repositorioTurno.findByProfesionalAndFechaBetweenOrderByFechaAscHoraInicioAsc(
                profesional, fechaInicio, fechaFin);

        // Filtrar solo estados conflictivos
        List<Turno> turnosConflictivos = turnosEnRango.stream()
                .filter(t -> t.getEstado() == EstadoTurno.CREADO || 
                             t.getEstado() == EstadoTurno.PENDIENTE_CONFIRMACION || 
                             t.getEstado() == EstadoTurno.CONFIRMADO)
                .collect(Collectors.toList());

        ConflictosBloqueoResponse response = new ConflictosBloqueoResponse();
        response.setTieneConflictos(!turnosConflictivos.isEmpty());
        response.setCantidadConflictos(turnosConflictivos.size());

        if (!turnosConflictivos.isEmpty()) {
            List<ConflictoTurnoDTO> conflictos = turnosConflictivos.stream()
                    .map(this::convertirAConflictoDTO)
                    .collect(Collectors.toList());
            response.setTurnosConflictivos(conflictos);
            response.setMensaje("Se encontraron " + turnosConflictivos.size() + 
                    " turno(s) que deben ser resueltos antes de crear el bloqueo.");
        } else {
            response.setMensaje("No hay conflictos con turnos existentes.");
        }

        return response;
    }

    /**
     * Crear bloqueo con resolución de conflictos
     */
    @Transactional
    public BloqueoFechaResponse crearBloqueoConResolucion(String emailProfesional, ResolucionConflictoRequest request) {
        Profesional profesional = obtenerProfesional(emailProfesional);

        // Verificar conflictos nuevamente
        ConflictosBloqueoResponse conflictos = verificarConflictosConTurnos(profesional, request.getBloqueo());
        
        if (!conflictos.isTieneConflictos()) {
            // No hay conflictos, crear normalmente
            return crearBloqueoSinValidacionConflictos(profesional, request.getBloqueo());
        }

        // Resolver según la acción elegida
        switch (request.getAccion()) {
            case CANCELAR_TODOS:
                cancelarTodosLosTurnos(conflictos.getTurnosConflictivos());
                break;
                
            case REPROGRAMAR:
                if (request.getReprogramaciones() == null || request.getReprogramaciones().isEmpty()) {
                    throw new ValidacionException("Debe proporcionar las reprogramaciones para todos los turnos");
                }
                reprogramarTurnos(profesional, request.getReprogramaciones(), request.getBloqueo());
                break;
                
            case CANCELAR_FUTUROS:
                cancelarSoloTurnosFuturos(conflictos.getTurnosConflictivos());
                break;
                
            default:
                throw new ValidacionException("Acción no válida");
        }

        // Crear el bloqueo
        return crearBloqueoSinValidacionConflictos(profesional, request.getBloqueo());
    }

    /**
     * Sugerir slots disponibles para reprogramar un turno
     */
    @Transactional(readOnly = true)
    public List<SlotDisponibleDTO> sugerirSlotsDisponibles(String emailProfesional, Long turnoId, int diasABuscar) {
        Profesional profesional = obtenerProfesional(emailProfesional);
        
        // Obtener el turno
        Turno turno = repositorioTurno.findById(turnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        if (!turno.getProfesional().getId().equals(profesional.getId())) {
            throw new AccesoDenegadoException("No puede reprogramar turnos de otro profesional");
        }

        List<SlotDisponibleDTO> slots = new ArrayList<>();
        LocalDate fechaBusqueda = LocalDate.now();
        LocalDate fechaLimite = fechaBusqueda.plusDays(diasABuscar);
        
        // Obtener tiempo mínimo de anticipación de la empresa
        int tiempoMinimoAnticipacion = profesional.getEmpresa().getTiempoMinimoAnticipacionMinutos() != null ?
            profesional.getEmpresa().getTiempoMinimoAnticipacionMinutos() : 30;
        LocalTime horaLimiteHoy = LocalTime.now().plusMinutes(tiempoMinimoAnticipacion);

        // Obtener disponibilidades del profesional
        List<DisponibilidadProfesional> disponibilidades = repositorioDisponibilidadProfesional.findByProfesionalAndActivoTrue(profesional);

        while (fechaBusqueda.isBefore(fechaLimite) && slots.size() < 20) { // Máximo 20 sugerencias
            final LocalDate fechaActual = fechaBusqueda;
            final boolean esHoy = fechaActual.isEqual(LocalDate.now());
            
            // Convertir DayOfWeek a DiaSemana
            DiaSemana diaSemana = convertirDayOfWeekADiaSemana(fechaActual.getDayOfWeek());
            
            // Buscar disponibilidad para ese día
            List<DisponibilidadProfesional> disponibilidadesDia = disponibilidades.stream()
                    .filter(d -> d.getDiaSemana() == diaSemana)
                    .collect(Collectors.toList());

            // Para cada franja horaria disponible, generar slots
            for (DisponibilidadProfesional disp : disponibilidadesDia) {
                LocalTime horaActual = disp.getHoraInicio();
                while (horaActual.plusMinutes(turno.getDuracionMinutos()).isBefore(disp.getHoraFin()) ||
                       horaActual.plusMinutes(turno.getDuracionMinutos()).equals(disp.getHoraFin())) {
                    
                    LocalTime horaFin = horaActual.plusMinutes(turno.getDuracionMinutos());
                    
                    // Si es hoy, verificar que el slot esté después del tiempo mínimo de anticipación
                    if (esHoy && horaActual.isBefore(horaLimiteHoy)) {
                        horaActual = horaActual.plusMinutes(30);
                        continue;
                    }
                    
                    // Verificar que no haya turnos en ese slot
                    final LocalTime horaInicioFinal = horaActual;
                    boolean ocupado = repositorioTurno.existeSolapamiento(profesional, fechaActual, horaInicioFinal, horaFin);
                    
                    if (!ocupado) {
                        SlotDisponibleDTO slot = new SlotDisponibleDTO();
                        slot.setFecha(fechaActual);
                        slot.setHoraInicio(horaInicioFinal);
                        slot.setHoraFin(horaFin);
                        slot.setDia(fechaActual.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.of("es")));
                        slots.add(slot);
                    }
                    
                    horaActual = horaActual.plusMinutes(30); // Intervalos de 30 minutos
                }
            }

            fechaBusqueda = fechaBusqueda.plusDays(1);
        }

        return slots;
    }

    private DiaSemana convertirDayOfWeekADiaSemana(java.time.DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> DiaSemana.LUNES;
            case TUESDAY -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY -> DiaSemana.JUEVES;
            case FRIDAY -> DiaSemana.VIERNES;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }

    private BloqueoFechaResponse crearBloqueoSinValidacionConflictos(Profesional profesional, BloqueoFechaRequest request) {
        BloqueoFecha bloqueo = new BloqueoFecha();
        bloqueo.setProfesional(profesional);
        bloqueo.setFechaInicio(request.getFechaInicio());
        bloqueo.setFechaFin(request.getFechaFin());
        bloqueo.setMotivo(request.getMotivo());
        bloqueo.setActivo(true);

        bloqueo = repositorioBloqueoFecha.save(bloqueo);
        return convertirAResponse(bloqueo);
    }

    private void cancelarTodosLosTurnos(List<ConflictoTurnoDTO> conflictos) {
        for (ConflictoTurnoDTO conflicto : conflictos) {
            Turno turno = repositorioTurno.findById(conflicto.getTurnoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));
            
            turno.setEstado(EstadoTurno.CANCELADO);
            turno.setMotivoCancelacion("Cancelado por creación de bloqueo de fecha");
            turno.setCanceladoPor("PROFESIONAL");
            turno.setFechaCancelacion(java.time.LocalDateTime.now());
            repositorioTurno.save(turno);
        }
    }

    private void cancelarSoloTurnosFuturos(List<ConflictoTurnoDTO> conflictos) {
        LocalDate hoy = LocalDate.now();
        
        for (ConflictoTurnoDTO conflicto : conflictos) {
            // Solo cancelar turnos >= hoy
            if (conflicto.getFecha().isBefore(hoy)) {
                continue;
            }
            
            Turno turno = repositorioTurno.findById(conflicto.getTurnoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));
            
            turno.setEstado(EstadoTurno.CANCELADO);
            turno.setMotivoCancelacion("Cancelado por creación de bloqueo de fecha");
            turno.setCanceladoPor("PROFESIONAL");
            turno.setFechaCancelacion(java.time.LocalDateTime.now());
            repositorioTurno.save(turno);
        }
    }

    private void reprogramarTurnos(Profesional profesional, List<ReprogramacionTurnoDTO> reprogramaciones, 
                                   BloqueoFechaRequest bloqueoPendiente) {
        for (ReprogramacionTurnoDTO reprog : reprogramaciones) {
            Turno turno = repositorioTurno.findById(reprog.getTurnoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Turno " + reprog.getTurnoId() + " no encontrado"));

            // Validar que el turno pertenece al profesional
            if (!turno.getProfesional().getId().equals(profesional.getId())) {
                throw new ValidacionException("El turno " + reprog.getTurnoId() + " no pertenece al profesional");
            }

            // Validar que la nueva fecha no esté en el pasado
            if (reprog.getNuevaFecha().isBefore(LocalDate.now())) {
                throw new ValidacionException("No se puede reprogramar a una fecha pasada");
            }
            
            // Validar tiempo mínimo de anticipación para el día actual
            if (reprog.getNuevaFecha().isEqual(LocalDate.now())) {
                int tiempoMinimo = profesional.getEmpresa().getTiempoMinimoAnticipacionMinutos() != null ?
                    profesional.getEmpresa().getTiempoMinimoAnticipacionMinutos() : 30;
                LocalTime horaLimite = LocalTime.now().plusMinutes(tiempoMinimo);
                if (reprog.getNuevaHora().isBefore(horaLimite)) {
                    throw new ValidacionException("No se puede reprogramar para hoy con menos de " + tiempoMinimo + 
                        " minutos de anticipación. Hora mínima: " + horaLimite.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                }
            }

            // Validar que la nueva fecha no esté dentro del bloqueo
            LocalDate fechaFinBloqueo = bloqueoPendiente.getFechaFin() != null ? 
                    bloqueoPendiente.getFechaFin() : bloqueoPendiente.getFechaInicio();
            
            if (!reprog.getNuevaFecha().isBefore(bloqueoPendiente.getFechaInicio()) && 
                !reprog.getNuevaFecha().isAfter(fechaFinBloqueo)) {
                throw new ValidacionException("No se puede reprogramar a una fecha dentro del bloqueo");
            }

            LocalTime nuevaHoraFin = reprog.getNuevaHora().plusMinutes(turno.getDuracionMinutos());

            // Validar disponibilidad del profesional
            DiaSemana diaSemana = convertirDayOfWeekADiaSemana(reprog.getNuevaFecha().getDayOfWeek());
            List<DisponibilidadProfesional> disponibilidades = repositorioDisponibilidadProfesional
                    .findByProfesionalAndActivoTrue(profesional).stream()
                    .filter(d -> d.getDiaSemana() == diaSemana)
                    .collect(Collectors.toList());

            boolean dentroDisponibilidad = disponibilidades.stream().anyMatch(disp ->
                    !reprog.getNuevaHora().isBefore(disp.getHoraInicio()) &&
                    !nuevaHoraFin.isAfter(disp.getHoraFin())
            );

            if (!dentroDisponibilidad) {
                throw new ValidacionException("El horario " + reprog.getNuevaHora() + 
                        " no está dentro de la disponibilidad del profesional para el día " + 
                        reprog.getNuevaFecha());
            }

            // Validar solapamiento
            if (repositorioTurno.existeSolapamiento(profesional, reprog.getNuevaFecha(), 
                    reprog.getNuevaHora(), nuevaHoraFin)) {
                throw new ValidacionException("Ya existe un turno en el horario " + 
                        reprog.getNuevaFecha() + " " + reprog.getNuevaHora());
            }

            // Reprogramar el turno
            turno.setFecha(reprog.getNuevaFecha());
            turno.setHoraInicio(reprog.getNuevaHora());
            turno.setHoraFin(nuevaHoraFin);
            repositorioTurno.save(turno);
        }
    }

    private ConflictoTurnoDTO convertirAConflictoDTO(Turno turno) {
        ConflictoTurnoDTO dto = new ConflictoTurnoDTO();
        dto.setTurnoId(turno.getId());
        dto.setFecha(turno.getFecha());
        dto.setHoraInicio(turno.getHoraInicio());
        dto.setHoraFin(turno.getHoraFin());
        dto.setClienteNombre(turno.getCliente().getNombre());
        dto.setClienteTelefono(turno.getCliente().getTelefono());
        dto.setServicioNombre(turno.getServicio().getNombre());
        dto.setEstado(turno.getEstado());
        return dto;
    }

    @Transactional
    public BloqueoFechaResponse actualizarBloqueo(String emailProfesional, Long bloqueoId, 
                                                   BloqueoFechaRequest request) {
        // Validar fechas
        if (request.getFechaFin() != null && request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior o igual a la fecha de inicio");
        }

        // Obtener profesional
        Profesional profesional = obtenerProfesional(emailProfesional);

        // Obtener bloqueo y validar pertenencia
        BloqueoFecha bloqueo = repositorioBloqueoFecha.findById(bloqueoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Bloqueo no encontrado"));

        if (!bloqueo.getProfesional().getId().equals(profesional.getId())) {
            throw new AccesoDenegadoException("No puede modificar el bloqueo de otro profesional");
        }

        // Verificar solapamientos (excluyendo el bloqueo actual)
        List<BloqueoFecha> bloqueosExistentes;
        if (request.getFechaFin() != null) {
            bloqueosExistentes = repositorioBloqueoFecha.findBloqueosEnRango(
                    profesional, request.getFechaInicio(), request.getFechaFin());
        } else {
            bloqueosExistentes = repositorioBloqueoFecha.findBloqueoEnFecha(
                    profesional, request.getFechaInicio());
        }

        // Filtrar el bloqueo actual
        bloqueosExistentes = bloqueosExistentes.stream()
                .filter(b -> !b.getId().equals(bloqueoId))
                .collect(Collectors.toList());

        if (!bloqueosExistentes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ya existe un bloqueo que se solapa con las fechas indicadas");
        }

        // Actualizar bloqueo
        bloqueo.setFechaInicio(request.getFechaInicio());
        bloqueo.setFechaFin(request.getFechaFin());
        bloqueo.setMotivo(request.getMotivo());

        bloqueo = repositorioBloqueoFecha.save(bloqueo);

        return convertirAResponse(bloqueo);
    }

    @Transactional(readOnly = true)
    public List<BloqueoFechaResponse> obtenerBloqueosPropios(String emailProfesional) {
        Profesional profesional = obtenerProfesional(emailProfesional);

        List<BloqueoFecha> bloqueos = 
                repositorioBloqueoFecha.findByProfesionalAndActivoTrueOrderByFechaInicioAsc(profesional);

        return bloqueos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarBloqueo(String emailProfesional, Long bloqueoId) {
        // Obtener profesional
        Profesional profesional = obtenerProfesional(emailProfesional);

        // Obtener bloqueo y validar pertenencia
        BloqueoFecha bloqueo = repositorioBloqueoFecha.findById(bloqueoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Bloqueo no encontrado"));

        if (!bloqueo.getProfesional().getId().equals(profesional.getId())) {
            throw new AccesoDenegadoException("No puede eliminar el bloqueo de otro profesional");
        }

        // Soft delete
        bloqueo.setActivo(false);
        repositorioBloqueoFecha.save(bloqueo);
    }

    /**
     * CORREGIDO: Ahora valida profesional activo + empresa activa
     */
    private Profesional obtenerProfesional(String email) {
        return servicioValidacionProfesional.validarYObtenerProfesional(email);
    }

    private BloqueoFechaResponse convertirAResponse(BloqueoFecha bloqueo) {
        BloqueoFechaResponse response = new BloqueoFechaResponse();
        response.setId(bloqueo.getId());
        response.setProfesionalId(bloqueo.getProfesional().getId());
        response.setFechaInicio(bloqueo.getFechaInicio());
        response.setFechaFin(bloqueo.getFechaFin());
        response.setMotivo(bloqueo.getMotivo());
        response.setActivo(bloqueo.getActivo());
        response.setFechaCreacion(bloqueo.getFechaCreacion());
        return response;
    }
}
