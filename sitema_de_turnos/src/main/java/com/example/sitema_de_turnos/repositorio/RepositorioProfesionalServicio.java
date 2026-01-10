package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.Profesional;
import com.example.sitema_de_turnos.modelo.ProfesionalServicio;
import com.example.sitema_de_turnos.modelo.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @deprecated Funcionalidad redundante con Profesional.serviciosBloqueados.
 * Se mantiene por compatibilidad con datos existentes. Pendiente de eliminación.
 */
@Deprecated(since = "1.0", forRemoval = true)
@SuppressWarnings("removal")
@Repository
public interface RepositorioProfesionalServicio extends JpaRepository<ProfesionalServicio, Long> {
    
    @Deprecated(since = "1.0", forRemoval = true)
    Optional<ProfesionalServicio> findByProfesionalAndServicio(Profesional profesional, Servicio servicio);
    
    @Deprecated(since = "1.0", forRemoval = true)
    List<ProfesionalServicio> findByProfesional(Profesional profesional);
    
    @Deprecated(since = "1.0", forRemoval = true)
    List<ProfesionalServicio> findByServicio(Servicio servicio);
}
