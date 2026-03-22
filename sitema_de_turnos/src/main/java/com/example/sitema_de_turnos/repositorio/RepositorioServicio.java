package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.Servicio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioServicio extends JpaRepository<Servicio, Long> {

    @EntityGraph(attributePaths = {"empresa"})
    List<Servicio> findByEmpresa(Empresa empresa);

    Optional<Servicio> findFirstByEmpresaAndNombre(Empresa empresa, String nombre);

    Optional<Servicio> findFirstByEmpresaAndNombreAndIdNot(Empresa empresa, String nombre, Long id);
    
    @EntityGraph(attributePaths = {"empresa"})
    List<Servicio> findByEmpresaAndActivoTrue(Empresa empresa);
}
