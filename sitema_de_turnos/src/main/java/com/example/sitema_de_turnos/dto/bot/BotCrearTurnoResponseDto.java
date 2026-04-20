package com.example.sitema_de_turnos.dto.bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotCrearTurnoResponseDto {

    private Long turnoId;
    private Long tenantId;
    private Long servicioId;
    private Long profesionalId;
    private String estado;
    private String fecha;
    private String horaInicio;
    private String horaFin;
}
