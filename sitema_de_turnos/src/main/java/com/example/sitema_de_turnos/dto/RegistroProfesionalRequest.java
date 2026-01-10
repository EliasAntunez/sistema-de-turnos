package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroProfesionalRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String email;

    @Size(max = 255, message = "La contraseña no puede exceder 255 caracteres")
    private String contrasena; // Opcional en actualización. La validación de longitud mínima se hace en el servicio

    @Pattern(regexp = "^$|^\\d{10,15}$", message = "El teléfono debe estar vacío o contener entre 10 y 15 dígitos numéricos")
    private String telefono; // Opcional

    @NotNull(message = "Debe especificar al menos una especialidad")
    @Size(min = 1, message = "Debe especificar al menos una especialidad")
    private List<String> especialidades;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
}
