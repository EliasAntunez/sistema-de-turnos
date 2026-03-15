package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarEmpresaRequest {

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(min = 2, max = 200, message = "El nombre debe tener entre 2 y 200 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccion;

    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String ciudad;

    @Size(max = 50, message = "La provincia no puede exceder 50 caracteres")
    private String provincia;

    @Pattern(regexp = "^$|^\\d{10,15}$", message = "El teléfono debe contener entre 10 y 15 dígitos numéricos")
    private String telefono;

    @Email(message = "El email debe ser válido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String email;
}