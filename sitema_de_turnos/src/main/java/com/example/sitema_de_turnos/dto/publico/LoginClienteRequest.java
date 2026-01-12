package com.example.sitema_de_turnos.dto.publico;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LoginClienteRequest {

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{8,20}$", message = "Formato de teléfono inválido")
    private String telefono;

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;
}
