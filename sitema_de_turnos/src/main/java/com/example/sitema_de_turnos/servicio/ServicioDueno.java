package com.example.sitema_de_turnos.servicio;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.sitema_de_turnos.modelo.PerfilDueno;
import com.example.sitema_de_turnos.repositorio.RepositorioPerfilDueno;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioDueno {
    private static final Logger logger = LoggerFactory.getLogger(ServicioDueno.class);
    private final RepositorioPerfilDueno repositorioPerfilDueno;

    @Transactional(readOnly = true)
    public PerfilDueno obtenerPorEmail(String email) {
        logger.debug("[DUENO] Buscando perfil de dueño por email: {}", email);
        if (email == null || email.isEmpty()) {
            logger.error("[DUENO] Email recibido es nulo o vacío");
            throw new IllegalArgumentException("Email recibido es nulo o vacío");
        }
        PerfilDueno perfil = repositorioPerfilDueno.findByUsuarioEmail(email)
                .orElseThrow(() -> {
                    logger.warn("[DUENO] No se encontró perfil de dueño con email: {}", email);
                    return new com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException(
                            "No se encontró perfil de dueño con email: " + email);
                });
        logger.debug("[DUENO] Perfil de dueño encontrado: {} (usuario id: {})",
                perfil.getUsuario().getEmail(), perfil.getUsuario().getId());
        if (perfil.getEmpresa() == null) {
            logger.warn("[DUENO] El dueño con email {} no tiene empresa asociada", email);
            throw new com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException(
                    "El dueño con email " + email + " no tiene empresa asociada.");
        }
        logger.debug("[DUENO] Empresa asociada encontrada: id {}", perfil.getEmpresa().getId());
        return perfil;
    }
}
