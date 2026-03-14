package com.example.sitema_de_turnos.excepcion;

import java.util.List;

/**
 * Excepción lanzada cuando se intenta eliminar o modificar un horario
 * (de empresa o de profesional) y existen Turnos activos
 * (PENDIENTE_CONFIRMACION, PENDIENTE_PAGO o CONFIRMADO) que quedarían sin cobertura.
 *
 * El campo {@code turnosAfectados} contiene una descripción legible de cada
 * turno conflictivo para que el dueño o profesional sepa qué debe cancelar
 * o reprogramar antes de reintentar la operación.
 */
public class OperacionBloqueadaPorTurnosException extends RuntimeException {

    private final List<String> turnosAfectados;

    public OperacionBloqueadaPorTurnosException(String mensaje, List<String> turnosAfectados) {
        super(mensaje);
        this.turnosAfectados = turnosAfectados;
    }

    public List<String> getTurnosAfectados() {
        return turnosAfectados;
    }
}
