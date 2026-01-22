package com.example.sitema_de_turnos.util;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Utilitario para parsear el username creado para clientes: "cliente:{empresaSlug}:{telefono}"
 */
public class PrincipalUtils {

    @Data
    @AllArgsConstructor
    public static class ClientePrincipal {
        private String empresaSlug;
        private String telefono;
    }

    public static ClientePrincipal parseClienteUsername(String username) {
        if (username == null) return null;
        String[] parts = username.split(":", 3);
        if (parts.length != 3) return null;
        if (!"cliente".equals(parts[0])) return null;
        return new ClientePrincipal(parts[1], parts[2]);
    }
}
