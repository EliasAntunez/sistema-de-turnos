package com.example.sitema_de_turnos.dto;

import com.example.sitema_de_turnos.modelo.EstadoTurno;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ConflictoTurnoDTO {
    private Long turnoId;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String clienteNombre;
    private String clienteTelefono;
    private String servicioNombre;
    private EstadoTurno estado;
}
