package com.example.sitema_de_turnos.excepcion;

/**
 * Excepción lanzada cuando un usuario intenta acceder a un recurso
 * que no le pertenece o para el cual no tiene permisos
 */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
