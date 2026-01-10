package com.example.sitema_de_turnos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalServicioResponse {
    private Long servicioId;
    private String nombre;
    private String descripcion;
    private Integer duracionMinutos;
    private Double precio;
    private Boolean disponible; // true = profesional puede ofrecer este servicio, false = está desactivado
    private Boolean heredado; // true = disponible por especialidad, false = override explícito
}
