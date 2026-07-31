package com.example.sitema_de_turnos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WinBackConfigDto {
    private Boolean winBackHabilitado;
    
    @Min(value = 7, message = "El mínimo de inactividad debe ser 7 días")
    @Max(value = 365, message = "El máximo de inactividad admitido es 365 días")
    private Integer winBackDiasInactividad;

    @Min(value = 30, message = "El cooldown de spam debe ser de al menos 30 días")
    private Integer winBackDiasEsperaReenvio;

    @NotBlank(message = "La plantilla del mensaje es obligatoria")
    private String winBackMensajePlantilla;

    @Min(value = 1, message = "El descuento mínimo es 1%")
    @Max(value = 100, message = "El descuento máximo es 100%")
    private Integer winBackDescuentoPorcentaje;

    private Boolean botInactivo;
}
