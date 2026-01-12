package com.example.sitema_de_turnos.dto.publico;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {
    private Long id;
    private String nombre;
    private String telefono;
    private String email;
    private Long empresaId;
    private String empresaNombre;
}
