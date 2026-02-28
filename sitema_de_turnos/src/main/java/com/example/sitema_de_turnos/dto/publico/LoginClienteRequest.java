package com.example.sitema_de_turnos.dto.publico;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LoginClienteRequest {

    @NotBlank(message = "El email o nombre de usuario es obligatorio")
    @Size(max = 150, message = "El identificador no puede exceder 150 caracteres")
    private String identificador; // Puede ser email o nombreUsuario

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;
}
