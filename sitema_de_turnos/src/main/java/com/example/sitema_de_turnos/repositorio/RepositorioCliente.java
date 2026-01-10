package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.Cliente;
import com.example.sitema_de_turnos.modelo.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioCliente extends JpaRepository<Cliente, Long> {

    /**
     * Buscar cliente por teléfono dentro de una empresa
     */
    Optional<Cliente> findByEmpresaAndTelefonoAndActivoTrue(Empresa empresa, String telefono);

    /**
     * Buscar cliente por email dentro de una empresa
     */
    Optional<Cliente> findByEmpresaAndEmailAndActivoTrue(Empresa empresa, String email);

    /**
     * Listar todos los clientes activos de una empresa
     */
    List<Cliente> findByEmpresaAndActivoTrue(Empresa empresa);

    /**
     * Verificar si existe un teléfono en una empresa
     */
    boolean existsByEmpresaAndTelefonoAndActivoTrue(Empresa empresa, String telefono);
}
