package com.example.sitema_de_turnos.dto.bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotDisponibilidadResponseDto {

    private String fecha;
    private Long servicioId;
    private List<String> horariosDisponibles;
}
