package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.DispositivoToken;
import com.example.sitema_de_turnos.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioDispositivoToken extends JpaRepository<DispositivoToken, Long> {

    Optional<DispositivoToken> findByToken(String token);

    List<DispositivoToken> findByUsuario(Usuario usuario);

    List<DispositivoToken> findByUsuarioId(Long usuarioId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM DispositivoToken d WHERE d.token IN :tokens")
    int deleteByTokenIn(@Param("tokens") List<String> tokens);
}