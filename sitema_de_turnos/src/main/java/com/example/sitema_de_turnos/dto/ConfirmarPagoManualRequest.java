package com.example.sitema_de_turnos.dto;

import com.example.sitema_de_turnos.modelo.MetodoPago;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConfirmarPagoManualRequest {

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;
}
