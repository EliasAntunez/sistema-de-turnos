package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.PerfilProfesional;
import com.example.sitema_de_turnos.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioPerfilProfesional extends JpaRepository<PerfilProfesional, Long> {

    List<PerfilProfesional> findByEmpresa(Empresa empresa);

    List<PerfilProfesional> findByEmpresaAndActivoTrue(Empresa empresa);

    Optional<PerfilProfesional> findByUsuario(Usuario usuario);

    Optional<PerfilProfesional> findByUsuarioEmail(String email);

    /** Busca el perfil de un usuario en una empresa concreta (para el caso dueño+profesional). */
    Optional<PerfilProfesional> findByUsuarioAndEmpresa(Usuario usuario, Empresa empresa);

    boolean existsByUsuarioEmail(String email);

    boolean existsByUsuarioEmailAndEmpresaId(String email, Long empresaId);
}
