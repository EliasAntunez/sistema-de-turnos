package com.example.sitema_de_turnos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilUsuarioResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    /** @deprecated Usar roles. Mantenido por compatibilidad con frontend v1. */
    @Deprecated(forRemoval = false)
    private String rol;
    private List<String> roles;
    private Boolean activo;
    private Long empresaId;
    private String empresaNombre;
}
