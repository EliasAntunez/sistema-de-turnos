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

    /**
     * true si este turno está dentro del período de restricción
     * definido por la política de cancelación de la empresa.
     * Es solo INFORMATIVO: el profesional puede igualmente proceder.
     */
    private boolean violaPolitica = false;

    /**
     * Descripción legible de por qué viola la política.
     * Ej: "Faltan 3h para el turno. La política requiere 24h de anticipación."
     */
    private String descripcionViolacion;
}
