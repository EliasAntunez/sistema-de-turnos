package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.Profesional;
import com.example.sitema_de_turnos.modelo.ProfesionalServicio;
import com.example.sitema_de_turnos.modelo.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la tabla profesional_servicio que gestiona
 * qué servicios puede ofrecer cada profesional.
 */
@Repository
public interface RepositorioProfesionalServicio extends JpaRepository<ProfesionalServicio, Long> {
    
    Optional<ProfesionalServicio> findByProfesionalAndServicio(Profesional profesional, Servicio servicio);
    
    List<ProfesionalServicio> findByProfesionalAndActivoTrue(Profesional profesional);
}
