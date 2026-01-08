package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioProfesional extends JpaRepository<Profesional, Long> {
    
    List<Profesional> findByEmpresaId(Long empresaId);
    
    List<Profesional> findByEmpresa(Empresa empresa);
    
    Optional<Profesional> findByEmail(String email);
}
