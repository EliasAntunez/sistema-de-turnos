package com.example.sitema_de_turnos.dto.publico;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaReprogramarRequest {
    // Se reciben como strings en el JSON para evitar dependencias de jackson-jsr310 en tests
    private String fecha;
    private String horaInicio;
    private Long profesionalId; // opcional: si es nulo, se mantiene el mismo profesional

    public LocalDate getFechaAsLocalDate() {
        return LocalDate.parse(this.fecha, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public LocalTime getHoraInicioAsLocalTime() {
        return LocalTime.parse(this.horaInicio, DateTimeFormatter.ofPattern("HH:mm"));
    }
}
