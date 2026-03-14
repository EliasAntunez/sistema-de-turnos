package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.DisponibilidadResponse;
import com.example.sitema_de_turnos.dto.RegistroDisponibilidadRequest;
import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;
import com.example.sitema_de_turnos.excepcion.OperacionBloqueadaPorTurnosException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.modelo.*;
import com.example.sitema_de_turnos.repositorio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CORREGIDO: Ahora usa ServicioValidacionProfesional para validar empresa activa
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioDisponibilidad {

    private static final List<EstadoTurno> ESTADOS_ACTIVOS =
            List.of(EstadoTurno.PENDIENTE_CONFIRMACION, EstadoTurno.PENDIENTE_PAGO, EstadoTurno.CONFIRMADO);

    private final RepositorioDisponibilidadProfesional repositorioDisponibilidad;
    private final RepositorioHorarioEmpresa repositorioHorarioEmpresa;
    private final RepositorioTurno repositorioTurno;
    private final ServicioValidacionProfesional servicioValidacionProfesional;

    @Transactional
    public DisponibilidadResponse crearDisponibilidad(String emailProfesional, RegistroDisponibilidadRequest request) {
        // Validación de horarios
        if (request.getHoraFin().isBefore(request.getHoraInicio()) || 
            request.getHoraFin().equals(request.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // CORREGIDO: Validar profesional activo + empresa activa
        PerfilProfesional profesional = servicioValidacionProfesional.validarYObtenerProfesional(emailProfesional);

        // Validar que la disponibilidad esté dentro de los horarios de la empresa
        validarDentroHorarioEmpresa(profesional.getEmpresa(), request.getDiaSemana(), 
                request.getHoraInicio(), request.getHoraFin());

        // Verificar que el profesional no tenga overlap en el mismo día
        List<DisponibilidadProfesional> disponibilidadesExistentes = 
                repositorioDisponibilidad.findByProfesionalAndDiaSemana(profesional, request.getDiaSemana());

        for (DisponibilidadProfesional existente : disponibilidadesExistentes) {
            if (existente.getActivo()) {
                // Verificar overlap: nuevo inicio está entre existente O nuevo fin está entre existente O nuevo cubre existente
                boolean overlap = 
                    (request.getHoraInicio().isBefore(existente.getHoraFin()) && 
                     request.getHoraInicio().isAfter(existente.getHoraInicio())) ||
                    (request.getHoraFin().isAfter(existente.getHoraInicio()) && 
                     request.getHoraFin().isBefore(existente.getHoraFin())) ||
                    (request.getHoraInicio().isBefore(existente.getHoraInicio()) && 
                     request.getHoraFin().isAfter(existente.getHoraFin())) ||
                    (request.getHoraInicio().equals(existente.getHoraInicio()) || 
                     request.getHoraFin().equals(existente.getHoraFin()));

                if (overlap) {
                    throw new IllegalArgumentException(
                        String.format("Ya existe disponibilidad para %s entre %s y %s",
                            request.getDiaSemana(),
                            existente.getHoraInicio(),
                            existente.getHoraFin()));
                }
            }
        }

        // Crear disponibilidad
        DisponibilidadProfesional disponibilidad = new DisponibilidadProfesional();
        disponibilidad.setProfesional(profesional);
        disponibilidad.setDiaSemana(request.getDiaSemana());
        disponibilidad.setHoraInicio(request.getHoraInicio());
        disponibilidad.setHoraFin(request.getHoraFin());
        disponibilidad.setActivo(true);

        disponibilidad = repositorioDisponibilidad.save(disponibilidad);

        return convertirAResponse(disponibilidad);
    }

    @Transactional
    public DisponibilidadResponse actualizarDisponibilidad(String emailProfesional, Long disponibilidadId, 
                                                           RegistroDisponibilidadRequest request) {
        // Validación de horarios
        if (request.getHoraFin().isBefore(request.getHoraInicio()) || 
            request.getHoraFin().equals(request.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // CORREGIDO: Validar profesional activo + empresa activa
        PerfilProfesional profesional = servicioValidacionProfesional.validarYObtenerProfesional(emailProfesional);

        // Obtener disponibilidad y validar pertenencia
        DisponibilidadProfesional disponibilidad = repositorioDisponibilidad.findById(disponibilidadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Disponibilidad no encontrada"));

        if (!disponibilidad.getProfesional().getId().equals(profesional.getId())) {
            throw new AccesoDenegadoException("No puede modificar la disponibilidad de otro profesional");
        }

        // Validar que la disponibilidad esté dentro de los horarios de la empresa
        validarDentroHorarioEmpresa(profesional.getEmpresa(), request.getDiaSemana(), 
                request.getHoraInicio(), request.getHoraFin());

        // Verificar overlap con otras disponibilidades (excluyendo la actual)
        List<DisponibilidadProfesional> disponibilidadesExistentes = 
                repositorioDisponibilidad.findByProfesionalAndDiaSemana(profesional, request.getDiaSemana());

        for (DisponibilidadProfesional existente : disponibilidadesExistentes) {
            if (existente.getActivo() && !existente.getId().equals(disponibilidadId)) {
                boolean overlap = 
                    (request.getHoraInicio().isBefore(existente.getHoraFin()) && 
                     request.getHoraInicio().isAfter(existente.getHoraInicio())) ||
                    (request.getHoraFin().isAfter(existente.getHoraInicio()) && 
                     request.getHoraFin().isBefore(existente.getHoraFin())) ||
                    (request.getHoraInicio().isBefore(existente.getHoraInicio()) && 
                     request.getHoraFin().isAfter(existente.getHoraFin())) ||
                    (request.getHoraInicio().equals(existente.getHoraInicio()) || 
                     request.getHoraFin().equals(existente.getHoraFin()));

                if (overlap) {
                    throw new IllegalArgumentException(
                        String.format("Ya existe disponibilidad para %s entre %s y %s",
                            request.getDiaSemana(),
                            existente.getHoraInicio(),
                            existente.getHoraFin()));
                }
            }
        }

        // Verificar turnos activos que quedarían sin cobertura tras la actualización.
        // En este punto `disponibilidad` aún tiene los valores ANTERIORES (antes de setters).
        DayOfWeek diaAnteriorJava = disponibilidad.getDiaSemana().toDayOfWeek();
        List<Turno> candidatos = repositorioTurno
                .findActivosFuturosEnRangoPorProfesional(profesional, LocalDate.now(), ESTADOS_ACTIVOS,
                        disponibilidad.getHoraInicio(), disponibilidad.getHoraFin())
                .stream()
                .filter(t -> t.getFecha().getDayOfWeek() == diaAnteriorJava)
                .collect(Collectors.toList());

        List<Turno> turnosConflicto;
        if (!disponibilidad.getDiaSemana().equals(request.getDiaSemana())) {
            // Cambio de día: todos los turnos del día anterior quedan huérfanos
            turnosConflicto = candidatos;
        } else {
            // Mismo día: solo los que no caben en el nuevo rango
            turnosConflicto = candidatos.stream()
                    .filter(t -> t.getHoraInicio().isBefore(request.getHoraInicio())
                              || t.getHoraFin().isAfter(request.getHoraFin()))
                    .collect(Collectors.toList());
        }

        if (!turnosConflicto.isEmpty()) {
            List<String> descripciones = formatearTurnosAfectados(turnosConflicto);
            log.warn("[Disponibilidad] Modificación bloqueada por turnos – profesional_id={}, disponibilidad_id={}, turnos={}",
                    profesional.getId(), disponibilidadId, turnosConflicto.size());
            throw new OperacionBloqueadaPorTurnosException(
                    "No se puede modificar la disponibilidad porque existen " + turnosConflicto.size() +
                    " turno(s) activo(s) que quedarían sin cobertura. " +
                    "Cancele o reprograme esos turnos primero.",
                    descripciones);
        }

        // Actualizar disponibilidad
        disponibilidad.setDiaSemana(request.getDiaSemana());
        disponibilidad.setHoraInicio(request.getHoraInicio());
        disponibilidad.setHoraFin(request.getHoraFin());

        disponibilidad = repositorioDisponibilidad.save(disponibilidad);

        return convertirAResponse(disponibilidad);
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadResponse> obtenerDisponibilidadPropia(String emailProfesional) {
        // CORREGIDO: Validar profesional activo + empresa activa
        PerfilProfesional profesional = servicioValidacionProfesional.validarYObtenerProfesional(emailProfesional);

        List<DisponibilidadProfesional> disponibilidades = 
                repositorioDisponibilidad.findByProfesionalAndActivoTrue(profesional);

        return disponibilidades.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Elimina una disponibilidad del profesional.
     * Reglas de eliminación:
     * - Si NO tiene turnos asociados → DELETE físico de la base de datos
     * - Si tiene turnos terminados (pasados) → Desactivar (soft delete: activo = false)
     * - Si tiene turnos actuales/futuros → No permitir eliminación (lanzar excepción)
     *
     * NOTA: Cuando se implemente el modelo Turno, descomentar la lógica de validación.
     */
    @Transactional
    public void eliminarDisponibilidad(String emailProfesional, Long disponibilidadId) {
        // CORREGIDO: Validar profesional activo + empresa activa
        PerfilProfesional profesional = servicioValidacionProfesional.validarYObtenerProfesional(emailProfesional);

        // Obtener disponibilidad y validar pertenencia
        DisponibilidadProfesional disponibilidad = repositorioDisponibilidad.findById(disponibilidadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Disponibilidad no encontrada"));

        if (!disponibilidad.getProfesional().getId().equals(profesional.getId())) {
            throw new AccesoDenegadoException("No puede eliminar la disponibilidad de otro profesional");
        }

        // Verificar turnos activos que quedarían sin cobertura al eliminar esta disponibilidad
        DayOfWeek diaJava = disponibilidad.getDiaSemana().toDayOfWeek();
        List<Turno> turnosAfectados = repositorioTurno
                .findActivosFuturosEnRangoPorProfesional(profesional, LocalDate.now(), ESTADOS_ACTIVOS,
                        disponibilidad.getHoraInicio(), disponibilidad.getHoraFin())
                .stream()
                .filter(t -> t.getFecha().getDayOfWeek() == diaJava)
                .collect(Collectors.toList());

        if (!turnosAfectados.isEmpty()) {
            List<String> descripciones = formatearTurnosAfectados(turnosAfectados);
            log.warn("[Disponibilidad] Eliminación bloqueada por turnos – profesional_id={}, disponibilidad_id={}, turnos={}",
                    profesional.getId(), disponibilidadId, turnosAfectados.size());
            throw new OperacionBloqueadaPorTurnosException(
                    "No se puede eliminar la disponibilidad porque existen " + turnosAfectados.size() +
                    " turno(s) activo(s) programados en ese horario. " +
                    "Cancele o reprograme esos turnos primero.",
                    descripciones);
        }

        repositorioDisponibilidad.delete(disponibilidad);
    }

    /**
     * Valida que la disponibilidad del profesional esté dentro de los horarios de la empresa.
     * La disponibilidad debe estar completamente contenida dentro de al menos un horario de empresa para ese día.
     */
    private void validarDentroHorarioEmpresa(Empresa empresa, DiaSemana diaSemana, 
                                             LocalTime horaInicio, LocalTime horaFin) {
        List<HorarioEmpresa> horariosEmpresa = 
                repositorioHorarioEmpresa.findByEmpresaAndDiaSemanaAndActivoTrue(empresa, diaSemana);

        if (horariosEmpresa.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("La empresa no tiene horarios configurados para %s. " +
                            "Solicite al dueño que configure los horarios de la empresa primero.", 
                            diaSemana));
        }

        // Verificar si la disponibilidad está contenida en algún horario de la empresa
        boolean dentroDeAlgunHorario = horariosEmpresa.stream().anyMatch(horario ->
                !horaInicio.isBefore(horario.getHoraInicio()) && 
                !horaFin.isAfter(horario.getHoraFin())
        );

        if (!dentroDeAlgunHorario) {
            String horariosDisponibles = horariosEmpresa.stream()
                    .map(h -> h.getHoraInicio() + " - " + h.getHoraFin())
                    .collect(Collectors.joining(", "));

            throw new IllegalArgumentException(
                    String.format("El horario %s - %s para %s está fuera del horario de la empresa. " +
                            "Horarios disponibles: %s",
                            horaInicio, horaFin, diaSemana, horariosDisponibles));
        }
    }

    /**
     * Obtiene la disponibilidad efectiva de un profesional para un día específico.
     * Si el profesional tiene DisponibilidadProfesional configurada para ese día, la retorna.
     * Si no tiene configuración propia, hereda los HorarioEmpresa como fallback.
     * 
     * Este método centraliza la regla de negocio de herencia de horarios y debe ser
     * usado en todos los cálculos de disponibilidad real (turnos, validaciones, reportes).
     * 
     * @return Lista de rangos horarios efectivos para el día (puede estar vacía si la empresa tampoco tiene horarios)
     */
    @Transactional(readOnly = true)
    public List<DisponibilidadResponse> obtenerDisponibilidadEfectiva(PerfilProfesional profesional, DiaSemana diaSemana) {
        // Primero, buscar disponibilidad explícita del profesional
        List<DisponibilidadProfesional> disponibilidadesPropias = 
                repositorioDisponibilidad.findByProfesionalAndDiaSemanaAndActivoTrue(profesional, diaSemana);

        // Si tiene configuración propia, usarla (es la fuente de verdad)
        if (!disponibilidadesPropias.isEmpty()) {
            return disponibilidadesPropias.stream()
                    .map(this::convertirAResponse)
                    .collect(Collectors.toList());
        }

        // Fallback: heredar horarios de la empresa
        List<HorarioEmpresa> horariosEmpresa = 
                repositorioHorarioEmpresa.findByEmpresaAndDiaSemanaAndActivoTrue(
                    profesional.getEmpresa(), diaSemana);

        // Convertir HorarioEmpresa a DisponibilidadResponse
        return horariosEmpresa.stream()
                .map(horario -> {
                    DisponibilidadResponse response = new DisponibilidadResponse();
                    response.setId(null); // No tiene ID porque no es una entidad DisponibilidadProfesional
                    response.setDiaSemana(horario.getDiaSemana());
                    response.setHoraInicio(horario.getHoraInicio());
                    response.setHoraFin(horario.getHoraFin());
                    response.setActivo(true);
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * Inicializa la disponibilidad del profesional copiando todos los horarios de la empresa.
     * Este método es atómico y transaccional: crea todas las disponibilidades en una sola operación.
     * Valida que el profesional no tenga disponibilidad previa para evitar duplicados.
     * 
     * @param emailProfesional Email del profesional autenticado
     * @return Número de disponibilidades creadas
     */
    @Transactional
    public int inicializarDesdeEmpresa(String emailProfesional) {
        // CORREGIDO: Validar profesional activo + empresa activa
        PerfilProfesional profesional = servicioValidacionProfesional.validarYObtenerProfesional(emailProfesional);

        // Verificar que no tenga disponibilidad previa
        List<DisponibilidadProfesional> disponibilidadesExistentes = 
                repositorioDisponibilidad.findByProfesional(profesional);

        if (!disponibilidadesExistentes.isEmpty()) {
            throw new IllegalArgumentException(
                "Ya tiene disponibilidad configurada. Elimine sus horarios actuales antes de inicializar desde la empresa.");
        }

        // Obtener horarios de la empresa
        List<HorarioEmpresa> horariosEmpresa = 
                repositorioHorarioEmpresa.findByEmpresaAndActivoTrue(profesional.getEmpresa());

        if (horariosEmpresa.isEmpty()) {
            throw new IllegalArgumentException(
                "La empresa no tiene horarios configurados. Solicite al dueño que configure los horarios primero.");
        }

        int disponibilidadesCreadas = 0;

        // Crear una DisponibilidadProfesional por cada HorarioEmpresa
        for (HorarioEmpresa horarioEmpresa : horariosEmpresa) {
            DisponibilidadProfesional disponibilidad = new DisponibilidadProfesional();
            disponibilidad.setProfesional(profesional);
            disponibilidad.setDiaSemana(horarioEmpresa.getDiaSemana());
            disponibilidad.setHoraInicio(horarioEmpresa.getHoraInicio());
            disponibilidad.setHoraFin(horarioEmpresa.getHoraFin());
            disponibilidad.setActivo(true);
            
            repositorioDisponibilidad.save(disponibilidad);
            disponibilidadesCreadas++;
        }

        return disponibilidadesCreadas;
    }

    private List<String> formatearTurnosAfectados(List<Turno> turnos) {
        return turnos.stream()
                .map(t -> String.format("Turno #%d – %s (%s - %s)",
                        t.getId(), t.getFecha(), t.getHoraInicio(), t.getHoraFin()))
                .collect(Collectors.toList());
    }

    private DisponibilidadResponse convertirAResponse(DisponibilidadProfesional disponibilidad) {
        DisponibilidadResponse response = new DisponibilidadResponse();
        response.setId(disponibilidad.getId());
        response.setDiaSemana(disponibilidad.getDiaSemana());
        response.setHoraInicio(disponibilidad.getHoraInicio());
        response.setHoraFin(disponibilidad.getHoraFin());
        response.setActivo(disponibilidad.getActivo());
        return response;
    }
}
