package com.example.sitema_de_turnos.excepcion;

public class EntidadInactivaExistenteException extends ConflictoException {

    private final Long entidadId;

    public EntidadInactivaExistenteException(String mensaje, Long entidadId) {
        super(mensaje);
        this.entidadId = entidadId;
    }

    public Long getEntidadId() {
        return entidadId;
    }
}
