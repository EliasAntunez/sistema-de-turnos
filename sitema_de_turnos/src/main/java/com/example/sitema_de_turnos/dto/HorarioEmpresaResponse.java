package com.example.sitema_de_turnos.dto;

import com.example.sitema_de_turnos.modelo.DiaSemana;
import lombok.Data;

import java.time.LocalTime;

@Data
public class HorarioEmpresaResponse {

    private Long id;
    private Long empresaId;
    private DiaSemana diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Boolean activo;
}
