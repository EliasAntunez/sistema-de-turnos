package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.PoliticaCancelacion;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.TipoPoliticaCancelacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RepositorioPoliticaCancelacion extends JpaRepository<PoliticaCancelacion, Long> {
    List<PoliticaCancelacion> findByEmpresa(Empresa empresa);

    List<PoliticaCancelacion> findByEmpresaAndActivaTrue(Empresa empresa);

    Optional<PoliticaCancelacion> findByEmpresaAndTipoAndActivaTrue(Empresa empresa, TipoPoliticaCancelacion tipo);

    @Query("SELECT p FROM PoliticaCancelacion p WHERE p.empresa = :empresa AND p.activa = true ORDER BY p.fechaCreacion DESC")
    List<PoliticaCancelacion> findAllActivasByEmpresaOrderByFechaCreacionDesc(@Param("empresa") Empresa empresa);
}
