package com.example.sitema_de_turnos.dto;

import com.example.sitema_de_turnos.modelo.DiaSemana;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CopiarHorarioRequest {

    @NotNull(message = "El día fuente es obligatorio")
    private DiaSemana diaFuente;

    @NotEmpty(message = "Debe especificar al menos un día destino")
    private List<DiaSemana> diasDestino;

    @NotNull(message = "Debe especificar si desea reemplazar horarios existentes")
    private Boolean reemplazar;
}
