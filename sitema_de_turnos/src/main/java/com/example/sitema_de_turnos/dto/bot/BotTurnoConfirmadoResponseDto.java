package com.example.sitema_de_turnos.dto.bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotTurnoConfirmadoResponseDto {
    private Long turnoId;
    private Long servicioId;
    private String servicioNombre;
    private Long profesionalId;
    private String profesionalNombre;
    private String fecha;
    private String horaInicio;
    private String horaFin;
    private BigDecimal precio;
    private String observaciones;
}
