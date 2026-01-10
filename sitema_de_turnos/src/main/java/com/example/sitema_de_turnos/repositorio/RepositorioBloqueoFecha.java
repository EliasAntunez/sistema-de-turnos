package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.BloqueoFecha;
import com.example.sitema_de_turnos.modelo.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RepositorioBloqueoFecha extends JpaRepository<BloqueoFecha, Long> {

    /**
     * Obtiene todos los bloqueos activos de un profesional ordenados por fecha
     */
    List<BloqueoFecha> findByProfesionalAndActivoTrueOrderByFechaInicioAsc(Profesional profesional);

    /**
     * Obtiene todos los bloqueos activos de un profesional (sin ordenar)
     */
    List<BloqueoFecha> findByProfesionalAndActivoTrue(Profesional profesional);

    /**
     * Obtiene todos los bloqueos de un profesional (activos e inactivos)
     */
    List<BloqueoFecha> findByProfesionalOrderByFechaInicioAsc(Profesional profesional);

    /**
     * Verifica si un profesional tiene bloqueos activos en una fecha específica
     * Considera:
     * - Bloqueos de un solo día (fecha_fin es null y fecha_inicio = fecha)
     * - Bloqueos de rango (fecha está entre fecha_inicio y fecha_fin)
     */
    @Query("""
        SELECT b FROM BloqueoFecha b 
        WHERE b.profesional = :profesional 
        AND b.activo = true 
        AND (
            (b.fechaFin IS NULL AND b.fechaInicio = :fecha) 
            OR 
            (b.fechaFin IS NOT NULL AND :fecha BETWEEN b.fechaInicio AND b.fechaFin)
        )
        """)
    List<BloqueoFecha> findBloqueoEnFecha(
            @Param("profesional") Profesional profesional,
            @Param("fecha") LocalDate fecha
    );

    /**
     * Obtiene bloqueos activos que se solapan con un rango de fechas
     */
    @Query("""
        SELECT b FROM BloqueoFecha b 
        WHERE b.profesional = :profesional 
        AND b.activo = true 
        AND (
            (b.fechaFin IS NULL AND b.fechaInicio BETWEEN :fechaInicio AND :fechaFin)
            OR
            (b.fechaFin IS NOT NULL AND (
                (b.fechaInicio BETWEEN :fechaInicio AND :fechaFin)
                OR (b.fechaFin BETWEEN :fechaInicio AND :fechaFin)
                OR (b.fechaInicio <= :fechaInicio AND b.fechaFin >= :fechaFin)
            ))
        )
        """)
    List<BloqueoFecha> findBloqueosEnRango(
            @Param("profesional") Profesional profesional,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );
}
