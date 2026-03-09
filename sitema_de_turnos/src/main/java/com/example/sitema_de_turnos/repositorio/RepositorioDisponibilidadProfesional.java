package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.DiaSemana;
import com.example.sitema_de_turnos.modelo.DisponibilidadProfesional;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface RepositorioDisponibilidadProfesional extends JpaRepository<DisponibilidadProfesional, Long> {
    
    List<DisponibilidadProfesional> findByProfesional(Profesional profesional);
    
    List<DisponibilidadProfesional> findByProfesionalAndActivoTrue(Profesional profesional);
    
    List<DisponibilidadProfesional> findByProfesionalAndDiaSemana(Profesional profesional, DiaSemana diaSemana);
    
    List<DisponibilidadProfesional> findByProfesionalAndDiaSemanaAndActivoTrue(Profesional profesional, DiaSemana diaSemana);

    /**
     * Retorna todas las disponibilidades activas de profesionales de la empresa,
     * para el día dado, cuyo rango está completamente contenido dentro del rango
     * [horaInicio, horaFin] del horario empresa.
     * Se usa para detectar conflictos antes de eliminar un HorarioEmpresa.
     */
    @Query("SELECT d FROM DisponibilidadProfesional d " +
           "WHERE d.profesional.empresa = :empresa " +
           "AND d.diaSemana = :diaSemana " +
           "AND d.activo = true " +
           "AND d.horaInicio >= :horaInicio " +
           "AND d.horaFin <= :horaFin")
    List<DisponibilidadProfesional> findConflictosPorEliminacion(
            @Param("empresa") Empresa empresa,
            @Param("diaSemana") DiaSemana diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin);

    /**
     * Retorna disponibilidades activas dentro del rango anterior que quedan fuera del
     * nuevo rango en el mismo día (caso de achicamiento de franjas).
     * Solo aplica cuando el día no cambia; si el día cambia, usar findConflictosPorEliminacion.
     */
    @Query("SELECT d FROM DisponibilidadProfesional d " +
           "WHERE d.profesional.empresa = :empresa " +
           "AND d.diaSemana = :dia " +
           "AND d.activo = true " +
           "AND d.horaInicio >= :inicioAnterior " +
           "AND d.horaFin <= :finAnterior " +
           "AND NOT (d.horaInicio >= :inicioNuevo AND d.horaFin <= :finNuevo)")
    List<DisponibilidadProfesional> findConflictosPorAchicamiento(
            @Param("empresa") Empresa empresa,
            @Param("dia") DiaSemana dia,
            @Param("inicioAnterior") LocalTime inicioAnterior,
            @Param("finAnterior") LocalTime finAnterior,
            @Param("inicioNuevo") LocalTime inicioNuevo,
            @Param("finNuevo") LocalTime finNuevo);
}
