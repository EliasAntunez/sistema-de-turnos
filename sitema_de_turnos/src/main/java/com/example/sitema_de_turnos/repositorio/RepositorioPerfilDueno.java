package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.PerfilDueno;
import com.example.sitema_de_turnos.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioPerfilDueno extends JpaRepository<PerfilDueno, Long> {

    Optional<PerfilDueno> findByUsuario(Usuario usuario);

    Optional<PerfilDueno> findByUsuarioEmail(String email);

    boolean existsByUsuarioEmail(String email);

    boolean existsByDocumento(String documento);
}
