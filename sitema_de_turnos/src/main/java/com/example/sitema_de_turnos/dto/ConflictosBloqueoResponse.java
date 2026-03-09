package com.example.sitema_de_turnos.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConflictosBloqueoResponse {
    private boolean tieneConflictos;
    private int cantidadConflictos;
    private List<ConflictoTurnoDTO> turnosConflictivos;
    private String mensaje;

    /**
     * true si AL MENOS UN turno conflictivo viola la política de cancelación
     * de la empresa (ej: está a menos de 24hs del inicio).
     * Es solo INFORMATIVO: el frontend debe mostrar advertencia, no bloquear.
     */
    private boolean hayViolacionPolitica;
}
