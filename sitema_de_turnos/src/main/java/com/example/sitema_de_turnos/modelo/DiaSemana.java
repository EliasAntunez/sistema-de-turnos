package com.example.sitema_de_turnos.modelo;

import java.time.DayOfWeek;

public enum DiaSemana {
    LUNES,
    MARTES,
    MIERCOLES,
    JUEVES,
    VIERNES,
    SABADO,
    DOMINGO;

    /** Convierte este valor al {@link DayOfWeek} equivalente de Java. */
    public DayOfWeek toDayOfWeek() {
        return switch (this) {
            case LUNES     -> DayOfWeek.MONDAY;
            case MARTES    -> DayOfWeek.TUESDAY;
            case MIERCOLES -> DayOfWeek.WEDNESDAY;
            case JUEVES    -> DayOfWeek.THURSDAY;
            case VIERNES   -> DayOfWeek.FRIDAY;
            case SABADO    -> DayOfWeek.SATURDAY;
            case DOMINGO   -> DayOfWeek.SUNDAY;
        };
    }
}
