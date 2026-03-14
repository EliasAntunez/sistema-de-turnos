package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepositorioPago extends JpaRepository<Pago, UUID> {

    Optional<Pago> findByTurnoId(Long turnoId);

    @Query("SELECT p FROM Pago p JOIN FETCH p.turno t WHERE t.id IN :turnoIds")
    List<Pago> findByTurnoIds(@Param("turnoIds") List<Long> turnoIds);
}
