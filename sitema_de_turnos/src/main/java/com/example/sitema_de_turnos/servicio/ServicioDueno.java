package com.example.sitema_de_turnos.servicio;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.sitema_de_turnos.modelo.Dueno;
import com.example.sitema_de_turnos.repositorio.RepositorioDueno;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioDueno {
    private static final Logger logger = LoggerFactory.getLogger(ServicioDueno.class);
    private final RepositorioDueno repositorioDueno;

    @Transactional(readOnly = true)
    public Dueno obtenerPorEmail(String email) {
        logger.debug("[DUENO] Buscando dueño por email: {}", email);
        if (email == null || email.isEmpty()) {
            logger.error("[DUENO] Email recibido es nulo o vacío");
            throw new IllegalArgumentException("Email recibido es nulo o vacío");
        }
        var duenoOpt = repositorioDueno.findByEmail(email);
        if (duenoOpt.isEmpty()) {
            logger.warn("[DUENO] No se encontró dueño con email: {}", email);
            throw new com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException("No se encontró dueño con email: " + email);
        }
        Dueno dueno = duenoOpt.get();
        logger.debug("[DUENO] Dueño encontrado: {} (id: {})", dueno.getEmail(), dueno.getId());
        if (dueno.getEmpresa() == null) {
            logger.warn("[DUENO] El dueño con email {} no tiene empresa asociada", email);
            throw new com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException("El dueño con email " + email + " no tiene empresa asociada.");
        }
        logger.debug("[DUENO] Empresa asociada encontrada: id {}", dueno.getEmpresa().getId());
        return dueno;
    }
}
