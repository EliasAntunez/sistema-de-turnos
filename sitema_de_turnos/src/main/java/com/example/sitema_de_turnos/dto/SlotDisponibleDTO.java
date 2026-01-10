package com.example.sitema_de_turnos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotDisponibleDTO {
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String dia; // "lun", "mar", etc. para mostrar en UI
}
