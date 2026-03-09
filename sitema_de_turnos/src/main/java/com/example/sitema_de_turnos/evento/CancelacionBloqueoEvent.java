package com.example.sitema_de_turnos.evento;

import com.example.sitema_de_turnos.dto.notificacion.CancelacionBloqueoData;

import java.util.List;

/**
 * Evento publicado cuando se cancelan turnos masivamente al crear un bloqueo de agenda.
 *
 * Se publica con ApplicationEventPublisher DENTRO de la transacción.
 * El listener @TransactionalEventListener(AFTER_COMMIT) garantiza que las
 * notificaciones solo se envían si la transacción committeó exitosamente:
 * si hay un rollback posterior (p.ej. violación de constraint), los emails
 * NO se envían y no hay inconsistencia entre DB y notificaciones.
 */
public class CancelacionBloqueoEvent {

    private final List<CancelacionBloqueoData> datos;

    public CancelacionBloqueoEvent(List<CancelacionBloqueoData> datos) {
        this.datos = List.copyOf(datos); // defensa inmutable
    }

    public List<CancelacionBloqueoData> getDatos() {
        return datos;
    }
}
