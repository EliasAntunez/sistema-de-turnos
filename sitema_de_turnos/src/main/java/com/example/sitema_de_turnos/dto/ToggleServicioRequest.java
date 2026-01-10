package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToggleServicioRequest {
    
    @NotNull(message = "El ID del servicio es requerido")
    private Long servicioId;
    
    @NotNull(message = "El estado de disponibilidad es requerido")
    private Boolean disponible;
}
