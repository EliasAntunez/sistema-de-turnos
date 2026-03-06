package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioServicio extends JpaRepository<Servicio, Long> {
    
    List<Servicio> findByEmpresa(Empresa empresa);
    
    List<Servicio> findByEmpresaAndActivoTrue(Empresa empresa);
}
