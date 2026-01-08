package com.example.sitema_de_turnos.excepcion;

/**
 * Excepción lanzada cuando se intenta operar con una cuenta o empresa desactivada
 */
public class CuentaDesactivadaException extends RuntimeException {
    public CuentaDesactivadaException(String mensaje) {
        super(mensaje);
    }
}
