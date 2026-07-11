package com.example.sitema_de_turnos.dto.bot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotCancelarTurnoRequestDto {

    @NotBlank(message = "telefono es obligatorio")
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{7,20}$", message = "telefono tiene formato inválido")
    private String telefono;

    private String motivo;
}
