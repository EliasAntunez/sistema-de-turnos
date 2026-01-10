package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioEspecialidad extends JpaRepository<Especialidad, Long> {
    
    Optional<Especialidad> findByNombreIgnoreCase(String nombre);
}
