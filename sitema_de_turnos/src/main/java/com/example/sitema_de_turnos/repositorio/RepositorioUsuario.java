package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.RolUsuario;
import com.example.sitema_de_turnos.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmail(String email);
    
    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE :rol MEMBER OF u.roles")
    long countByRol(@Param("rol") RolUsuario rol);
}
