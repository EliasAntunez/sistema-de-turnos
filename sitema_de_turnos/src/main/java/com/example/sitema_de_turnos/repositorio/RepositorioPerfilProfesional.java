package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.PerfilProfesional;
import com.example.sitema_de_turnos.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioPerfilProfesional extends JpaRepository<PerfilProfesional, Long> {

    /**
     * OPTIMIZADO: JOIN FETCH sobre usuario elimina N+1.
     * Sin esto: 1 query (perfiles) + N queries (usuario por perfil).
     * Con esto: 1 sola query con INNER JOIN.
     */
    @Query("SELECT pp FROM PerfilProfesional pp JOIN FETCH pp.usuario WHERE pp.empresa = :empresa")
    List<PerfilProfesional> findByEmpresa(@Param("empresa") Empresa empresa);

    /**
     * OPTIMIZADO: JOIN FETCH sobre usuario elimina N+1.
     * Usado en lista de profesionales activos por empresa (vista dueño y pública).
     */
    @Query("SELECT pp FROM PerfilProfesional pp JOIN FETCH pp.usuario WHERE pp.empresa = :empresa AND pp.activo = true")
    List<PerfilProfesional> findByEmpresaAndActivoTrue(@Param("empresa") Empresa empresa);

    Optional<PerfilProfesional> findByUsuario(Usuario usuario);

    Optional<PerfilProfesional> findByUsuarioEmail(String email);

    /** Busca el perfil de un usuario en una empresa concreta (para el caso dueño+profesional). */
    Optional<PerfilProfesional> findByUsuarioAndEmpresa(Usuario usuario, Empresa empresa);

    boolean existsByUsuarioEmail(String email);

    boolean existsByUsuarioEmailAndEmpresaId(String email, Long empresaId);
}
