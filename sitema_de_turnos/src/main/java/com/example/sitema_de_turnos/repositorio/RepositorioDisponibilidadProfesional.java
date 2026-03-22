package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.DiaSemana;
import com.example.sitema_de_turnos.modelo.DisponibilidadProfesional;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.PerfilProfesional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface RepositorioDisponibilidadProfesional extends JpaRepository<DisponibilidadProfesional, Long> {
    
       @EntityGraph(attributePaths = {"profesional"})
       List<DisponibilidadProfesional> findByProfesional(PerfilProfesional profesional);
    
       @EntityGraph(attributePaths = {"profesional"})
       List<DisponibilidadProfesional> findByProfesionalAndActivoTrue(PerfilProfesional profesional);
    
       @EntityGraph(attributePaths = {"profesional"})
       List<DisponibilidadProfesional> findByProfesionalAndDiaSemana(PerfilProfesional profesional, DiaSemana diaSemana);
    
       @EntityGraph(attributePaths = {"profesional"})
       List<DisponibilidadProfesional> findByProfesionalAndDiaSemanaAndActivoTrue(PerfilProfesional profesional, DiaSemana diaSemana);

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
