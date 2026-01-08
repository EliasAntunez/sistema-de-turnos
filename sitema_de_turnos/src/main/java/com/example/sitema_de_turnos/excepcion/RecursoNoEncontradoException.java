package com.example.sitema_de_turnos.excepcion;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
