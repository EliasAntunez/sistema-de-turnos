package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AgregarObservacionesRequest {
    
    @NotBlank(message = "Las observaciones no pueden estar vacías")
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;
}
