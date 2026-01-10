package com.example.sitema_de_turnos.dto.publico;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotDisponibleResponse {
    private LocalDateTime horaInicio;
    private LocalDateTime horaFin;
    private Long profesionalId;
    private String profesionalNombre;
}
