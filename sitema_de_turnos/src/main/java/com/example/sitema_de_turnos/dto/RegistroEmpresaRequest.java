package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroEmpresaRequest {

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;

    @NotBlank(message = "El slug es obligatorio")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "El slug solo puede contener letras minúsculas, números y guiones")
    @Size(min = 3, max = 100, message = "El slug debe tener entre 3 y 100 caracteres")
    private String slug;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotBlank(message = "El CUIT es obligatorio")
    @Pattern(regexp = "^\\d{11}$", message = "El CUIT debe contener exactamente 11 dígitos numéricos")
    private String cuit;

    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccion;

    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String ciudad;

    @Size(max = 50, message = "La provincia no puede exceder 50 caracteres")
    private String provincia;

    @Pattern(regexp = "^\\d{10,15}$", message = "El teléfono debe contener entre 10 y 15 dígitos numéricos")
    private String telefono;

    @Email(message = "El email debe ser válido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String email;

    @Min(value = 1, message = "Los días máximos de reserva deben ser al menos 1")
    @Max(value = 365, message = "Los días máximos de reserva no pueden exceder 365")
    private Integer diasMaximosReserva;
}
