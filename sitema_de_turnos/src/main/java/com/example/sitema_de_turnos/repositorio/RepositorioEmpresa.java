package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface RepositorioEmpresa extends JpaRepository<Empresa, Long> {
    
    boolean existsByCuit(String cuit);
    
    Optional<Empresa> findByCuit(String cuit);
    
    Optional<Empresa> findBySlugAndActivaTrue(String slug);
    
    long countByActiva(Boolean activa);

    // Agregá este método en tu repositorio de Empresa
    Optional<Empresa> findByDuenoId(Long duenoId);

    /**
     * Buscar todas las empresas activas.
     * Usado por el job de recordatorios para procesar empresas.
     */
    List<Empresa> findByActivaTrue();
}
