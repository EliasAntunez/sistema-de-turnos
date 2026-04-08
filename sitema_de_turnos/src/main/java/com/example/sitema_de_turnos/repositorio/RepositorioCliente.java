package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.Cliente;
import com.example.sitema_de_turnos.modelo.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * Busca un cliente con usuario (cuenta registrada) por empresa y teléfono.
     * Usado para autenticación de clientes.
     */
    Optional<Cliente> findByEmpresaAndTelefonoAndTieneUsuarioTrueAndActivoTrue(Empresa empresa, String telefono);

    /**
     * Buscar cliente por email dentro de una empresa
     */
    Optional<Cliente> findByEmpresaAndEmailAndActivoTrue(Empresa empresa, String email);

    /**
     * Buscar cliente por nombre de usuario dentro de una empresa
     */
    Optional<Cliente> findByEmpresaAndNombreUsuarioAndActivoTrue(Empresa empresa, String nombreUsuario);

    /**
     * Buscar cliente con usuario por email
     */
    Optional<Cliente> findByEmpresaAndEmailAndTieneUsuarioTrueAndActivoTrue(Empresa empresa, String email);

    /**
     * Buscar cliente con usuario por nombre de usuario
     */
    Optional<Cliente> findByEmpresaAndNombreUsuarioAndTieneUsuarioTrueAndActivoTrue(Empresa empresa, String nombreUsuario);

    /**
     * Buscar cliente con usuario por email o nombre de usuario
     * Útil para el login donde el usuario puede ingresar cualquiera de los dos
     */
    @Query("SELECT c FROM Cliente c JOIN FETCH c.empresa " +
        "WHERE c.empresa = :empresa AND c.activo = true AND c.tieneUsuario = true " +
        "AND (c.email = :identificador OR c.nombreUsuario = :identificador)")
    Optional<Cliente> findByEmpresaAndEmailOrNombreUsuarioAndTieneUsuarioTrueAndActivoTrue(
            @Param("empresa") Empresa empresa,
            @Param("identificador") String identificador);

        @Query("SELECT c FROM Cliente c WHERE c.empresa = :empresa AND c.tieneUsuario = true " +
            "AND (c.email = :identificador OR c.nombreUsuario = :identificador)")
        Optional<Cliente> findByEmpresaAndEmailOrNombreUsuarioAndTieneUsuarioTrue(
             @Param("empresa") Empresa empresa,
             @Param("identificador") String identificador);

    /**
     * Listar todos los clientes activos de una empresa
     */
    List<Cliente> findByEmpresaAndActivoTrue(Empresa empresa);

    Optional<Cliente> findByIdAndEmpresa(Long id, Empresa empresa);

    /**
     * Verificar si existe un teléfono en una empresa
     */
    boolean existsByEmpresaAndTelefonoAndActivoTrue(Empresa empresa, String telefono);

    /**
     * Verificar si existe un email en una empresa
     */
    boolean existsByEmpresaAndEmailAndActivoTrue(Empresa empresa, String email);

    /**
     * Verificar si existe un nombre de usuario en una empresa
     */
    boolean existsByEmpresaAndNombreUsuarioAndActivoTrue(Empresa empresa, String nombreUsuario);
}
