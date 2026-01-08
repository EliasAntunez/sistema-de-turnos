package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.Dueno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioDueno extends JpaRepository<Dueno, Long> {
    
    Optional<Dueno> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    boolean existsByDocumento(String documento);
}
