package com.example.sitema_de_turnos.excepcion;

public class ValidacionException extends RuntimeException {
    public ValidacionException(String mensaje) {
        super(mensaje);
    }

    public ValidacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
