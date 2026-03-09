package com.example.sitema_de_turnos.excepcion;

import java.util.List;

/**
 * Excepción lanzada cuando se intenta eliminar o modificar un HorarioEmpresa
 * que tiene DisponibilidadProfesional activa dentro de ese rango.
 */
public class HorarioEnUsoException extends RuntimeException {

    private final List<String> profesionalesAfectados;

    public HorarioEnUsoException(String mensaje, List<String> profesionalesAfectados) {
        super(mensaje);
        this.profesionalesAfectados = profesionalesAfectados;
    }

    public List<String> getProfesionalesAfectados() {
        return profesionalesAfectados;
    }
}
