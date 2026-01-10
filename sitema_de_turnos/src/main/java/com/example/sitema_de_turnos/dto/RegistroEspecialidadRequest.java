package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistroEspecialidadRequest {

    @NotBlank(message = "El nombre de la especialidad es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
}
