package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.BotConfiguracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioBotConfiguracion extends JpaRepository<BotConfiguracion, Long> {

    Optional<BotConfiguracion> findByInstanciaWhatsapp(String instanciaWhatsapp);
}
