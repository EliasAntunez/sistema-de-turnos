package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioProfesional extends JpaRepository<Profesional, Long> {
    
    List<Profesional> findByEmpresa(Empresa empresa);
    
    @Query("SELECT DISTINCT p FROM Profesional p LEFT JOIN FETCH p.especialidades WHERE p.empresa = :empresa")
    List<Profesional> findByEmpresaWithEspecialidades(@Param("empresa") Empresa empresa);
    
    @Query("SELECT p FROM Profesional p LEFT JOIN FETCH p.especialidades WHERE p.id = :id")
    Optional<Profesional> findByIdWithEspecialidades(@Param("id") Long id);
    
    Optional<Profesional> findByEmail(String email);
}
