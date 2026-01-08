package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioSuperAdmin extends JpaRepository<SuperAdmin, Long> {
}
