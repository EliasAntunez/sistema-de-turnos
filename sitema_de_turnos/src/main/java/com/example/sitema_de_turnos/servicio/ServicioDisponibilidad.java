package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.DisponibilidadResponse;
import com.example.sitema_de_turnos.dto.RegistroDisponibilidadRequest;
import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.modelo.*;
import com.example.sitema_de_turnos.repositorio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CORREGIDO: Ahora usa ServicioValidacionProfesional para validar empresa activa
 */
@Service
@RequiredArgsConstructor
public class ServicioDisponibilidad {

    private final RepositorioDisponibilidadProfesional repositorioDisponibilidad;
    private final RepositorioHorarioEmpresa repositorioHorarioEmpresa;
    private final ServicioValidacionProfesional servicioValidacionProfesional;

    @Transactional
    public DisponibilidadResponse crearDisponibilidad(String emailProfesional, RegistroDisponibilidadRequest request) {
        // Validación de horarios
        if (request.getHoraFin().isBefore(request.getHoraInicio()) || 
            request.getHoraFin().equals(request.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // CORREGIDO: Validar profesional activo + empresa activa
        Profesional profesional = servicioValidacionProfesional.validarYObtenerProfesional(emailProfesional);

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
        Profesional profesional = servicioValidacionProfesional.validarYObtenerProfesional(emailProfesional);

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
        Profesional profesional = servicioValidacionProfesional.validarYObtenerProfesional(emailProfesional);

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
        Profesional profesional = servicioValidacionProfesional.validarYObtenerProfesional(emailProfesional);

        // Obtener disponibilidad y validar pertenencia
        DisponibilidadProfesional disponibilidad = repositorioDisponibilidad.findById(disponibilidadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Disponibilidad no encontrada"));

        if (!disponibilidad.getProfesional().getId().equals(profesional.getId())) {
            throw new AccesoDenegadoException("No puede eliminar la disponibilidad de otro profesional");
        }

        // TODO: Cuando exista el modelo Turno, descomentar esta validación:
        /*
        List<Turno> turnos = repositorioTurno.findByDisponibilidadProfesionalId(disponibilidadId);
        
        if (!turnos.isEmpty()) {
            // Verificar si hay turnos futuros o actuales (pendientes)
            LocalDateTime ahora = LocalDateTime.now();
            boolean hasTurnosFuturos = turnos.stream()
                    .anyMatch(t -> t.getEstado() != EstadoTurno.COMPLETADO 
                                && t.getEstado() != EstadoTurno.CANCELADO
                                && t.getFechaHora().isAfter(ahora));
            
            if (hasTurnosFuturos) {
                throw new IllegalStateException(
                    "No se puede eliminar la disponibilidad porque tiene turnos pendientes o futuros. " +
                    "Debe cancelar o completar los turnos primero."
                );
            }
            
            // Si solo tiene turnos pasados/completados, hacer soft delete
            disponibilidad.setActivo(false);
            repositorioDisponibilidad.save(disponibilidad);
            return;
        }
        */
        
        // Mientras no exista Turno, hacer DELETE físico directamente
        // (Una vez implementado Turno, este código solo se ejecuta si no hay turnos asociados)
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
    public List<DisponibilidadResponse> obtenerDisponibilidadEfectiva(Profesional profesional, DiaSemana diaSemana) {
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
        Profesional profesional = servicioValidacionProfesional.validarYObtenerProfesional(emailProfesional);

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
