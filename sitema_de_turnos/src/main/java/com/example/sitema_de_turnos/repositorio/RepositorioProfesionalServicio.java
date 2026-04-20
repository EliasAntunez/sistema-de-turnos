package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.PerfilProfesional;
import com.example.sitema_de_turnos.modelo.ProfesionalServicio;
import com.example.sitema_de_turnos.modelo.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioProfesionalServicio extends JpaRepository<ProfesionalServicio, Long> {
    
    Optional<ProfesionalServicio> findByProfesionalAndServicio(PerfilProfesional profesional, Servicio servicio);
    
    List<ProfesionalServicio> findByProfesionalAndActivoTrue(PerfilProfesional profesional);

    @Query("SELECT ps.profesional FROM ProfesionalServicio ps " +
           "WHERE ps.servicio = :servicio " +
           "AND ps.activo = true " +
           "AND ps.profesional.empresa.id = :tenantId " +
           "AND ps.profesional.activo = true")
    List<PerfilProfesional> findProfesionalesActivosByServicioAndTenantId(
        @Param("servicio") Servicio servicio,
        @Param("tenantId") Long tenantId
    );
}
