package com.example.sitema_de_turnos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorValidacion {
    
    private String mensaje;
    private Map<String, String> errores;
    private LocalDateTime timestamp;

    public ErrorValidacion(String mensaje, Map<String, String> errores) {
        this.mensaje = mensaje;
        this.errores = errores;
        this.timestamp = LocalDateTime.now();
    }
}
