package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface RepositorioTurno extends JpaRepository<Turno, Long> {

    /**
     * Buscar turnos de un profesional en una fecha específica
     */
    List<Turno> findByProfesionalAndFechaAndEstadoNotOrderByHoraInicio(
        Profesional profesional, 
        LocalDate fecha, 
        EstadoTurno estado
    );

    /**
     * Buscar turnos de un cliente en una empresa
     */
    List<Turno> findByClienteAndEmpresaOrderByFechaDescHoraInicioDesc(Cliente cliente, Empresa empresa);

    /**
     * Buscar turnos de una empresa en un rango de fechas
     */
    List<Turno> findByEmpresaAndFechaBetweenOrderByFechaAscHoraInicioAsc(
        Empresa empresa, 
        LocalDate fechaInicio, 
        LocalDate fechaFin
    );

    /**
     * Verificar si existe un turno en un horario específico para un profesional
     * (excluyendo turnos cancelados)
     */
    @Query("SELECT COUNT(t) > 0 FROM Turno t WHERE t.profesional = :profesional " +
           "AND t.fecha = :fecha " +
           "AND t.estado NOT IN ('CANCELADO') " +
           "AND ((t.horaInicio < :horaFin AND t.horaFin > :horaInicio))")
    boolean existeSolapamiento(
        @Param("profesional") Profesional profesional,
        @Param("fecha") LocalDate fecha,
        @Param("horaInicio") LocalTime horaInicio,
        @Param("horaFin") LocalTime horaFin
    );

    /**
     * Buscar turnos activos de un profesional en una fecha
     */
    @Query("SELECT t FROM Turno t WHERE t.profesional = :profesional " +
           "AND t.fecha = :fecha " +
           "AND t.estado NOT IN ('CANCELADO') " +
           "ORDER BY t.horaInicio")
    List<Turno> findTurnosActivosByProfesionalAndFecha(
        @Param("profesional") Profesional profesional,
        @Param("fecha") LocalDate fecha
    );

    /**
     * Buscar turnos de un profesional en un rango de fechas
     */
    List<Turno> findByProfesionalAndFechaBetweenOrderByFechaAscHoraInicioAsc(
        Profesional profesional,
        LocalDate fechaInicio,
        LocalDate fechaFin
    );
}
