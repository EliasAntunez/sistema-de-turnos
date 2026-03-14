package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.BloqueoFecha;
import com.example.sitema_de_turnos.modelo.PerfilProfesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RepositorioBloqueoFecha extends JpaRepository<BloqueoFecha, Long> {

    List<BloqueoFecha> findByProfesionalAndActivoTrueOrderByFechaInicioAsc(PerfilProfesional profesional);

    List<BloqueoFecha> findByProfesionalAndActivoTrue(PerfilProfesional profesional);

    List<BloqueoFecha> findByProfesionalOrderByFechaInicioAsc(PerfilProfesional profesional);

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
            @Param("profesional") PerfilProfesional profesional,
            @Param("fecha") LocalDate fecha
    );

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
            @Param("profesional") PerfilProfesional profesional,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );
}
