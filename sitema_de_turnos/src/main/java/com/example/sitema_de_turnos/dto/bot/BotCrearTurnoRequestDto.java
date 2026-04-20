package com.example.sitema_de_turnos.dto.bot;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotCrearTurnoRequestDto {

    @NotBlank(message = "clienteNombre es obligatorio")
    private String clienteNombre;

    @NotBlank(message = "telefono es obligatorio")
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "telefono tiene formato inválido")
    private String telefono;

    @NotNull(message = "servicioId es obligatorio")
    private Long servicioId;

    @NotNull(message = "fechaHora es obligatoria")
    @FutureOrPresent(message = "fechaHora debe ser presente o futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaHora;
}
