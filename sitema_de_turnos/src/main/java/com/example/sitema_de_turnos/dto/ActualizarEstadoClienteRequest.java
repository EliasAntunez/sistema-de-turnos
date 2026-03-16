package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActualizarEstadoClienteRequest {

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;
}
