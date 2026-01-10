package com.example.sitema_de_turnos.dto.publico;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CrearTurnoRequest {

    @NotNull(message = "El ID del servicio es obligatorio")
    private Long servicioId;

    @NotNull(message = "El ID del profesional es obligatorio")
    private Long profesionalId;

    @NotBlank(message = "La fecha es obligatoria")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Formato de fecha inválido (yyyy-MM-dd)")
    private String fecha;

    @NotBlank(message = "La hora de inicio es obligatoria")
    @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "Formato de hora inválido (HH:mm)")
    private String horaInicio;

    // Datos del cliente
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombreCliente;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{8,20}$", message = "Formato de teléfono inválido")
    private String telefonoCliente;

    @Email(message = "Formato de email inválido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String emailCliente; // Opcional - puede ser null o vacío

    private String observaciones;
}
