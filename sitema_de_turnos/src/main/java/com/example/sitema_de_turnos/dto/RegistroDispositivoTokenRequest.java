package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroDispositivoTokenRequest {

    @NotBlank(message = "El token es obligatorio")
    @Size(max = 512, message = "El token no puede exceder 512 caracteres")
    private String token;

    @Size(max = 1000, message = "El userAgent no puede exceder 1000 caracteres")
    private String userAgent;
}