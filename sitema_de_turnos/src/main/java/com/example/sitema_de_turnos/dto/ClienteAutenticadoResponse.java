package com.example.sitema_de_turnos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteAutenticadoResponse {
    private Long id;
    private String nombre;
    private String telefono;
    private String email;
    private Long empresaId;
    private String empresaNombre;
}
