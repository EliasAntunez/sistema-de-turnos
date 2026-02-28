package com.example.sitema_de_turnos.dto.publico;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroClienteRequest {
    
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{3,50}$", message = "El nombre de usuario debe tener entre 3 y 50 caracteres y solo puede contener letras, números, puntos, guiones y guiones bajos")
    private String nombreUsuario;
    
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono; // Opcional - validación de formato en servicio
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "La contraseña debe contener al menos una mayúscula, una minúscula y un número"
    )
    private String contrasena;
    
    @NotBlank(message = "Debe confirmar la contraseña")
    private String confirmarContrasena;
}
