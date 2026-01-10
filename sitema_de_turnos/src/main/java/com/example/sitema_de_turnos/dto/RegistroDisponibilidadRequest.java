package com.example.sitema_de_turnos.dto;

import com.example.sitema_de_turnos.modelo.DiaSemana;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class RegistroDisponibilidadRequest {

    @NotNull(message = "El día de la semana es obligatorio")
    private DiaSemana diaSemana;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;
}
