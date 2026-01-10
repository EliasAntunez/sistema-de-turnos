package com.example.sitema_de_turnos.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConflictosBloqueoResponse {
    private boolean tieneConflictos;
    private int cantidadConflictos;
    private List<ConflictoTurnoDTO> turnosConflictivos;
    private String mensaje;
}
