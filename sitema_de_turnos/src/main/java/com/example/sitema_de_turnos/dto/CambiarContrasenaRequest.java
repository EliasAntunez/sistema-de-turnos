package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarContrasenaRequest {

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String contrasenaActual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 255, message = "La nueva contraseña debe tener al menos 8 caracteres")
    private String nuevaContrasena;

    @NotBlank(message = "La confirmación de la nueva contraseña es obligatoria")
    private String confirmarNuevaContrasena;
}
