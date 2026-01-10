package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class RegistroServicioRequest {

    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "La duración en minutos es obligatoria")
    @Positive(message = "La duración debe ser un número positivo")
    private Integer duracionMinutos;

    /**
     * Buffer en minutos después del servicio (opcional).
     * Si es null, usa el buffer por defecto de la empresa.
     */
    @Positive(message = "El buffer debe ser un número positivo")
    private Integer bufferMinutos;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser un número positivo")
    private BigDecimal precio;

    @NotNull(message = "Debe seleccionar al menos una especialidad")
    @Size(min = 1, message = "Debe seleccionar al menos una especialidad")
    private Set<String> especialidades; // Nombres de especialidades
}
