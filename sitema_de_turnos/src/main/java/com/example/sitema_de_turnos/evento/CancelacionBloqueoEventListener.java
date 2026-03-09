package com.example.sitema_de_turnos.evento;

import com.example.sitema_de_turnos.servicio.notificacion.ServicioNotificacionBloqueo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Escucha CancelacionBloqueoEvent y delega el envío de emails al servicio de notificaciones.
 *
 * Al ser un @Component diferente de quien publica el evento, la llamada a
 * servicioNotificacionBloqueo.notificarCancelacionPorBloqueo() pasa por el proxy
 * de Spring, de modo que @Async se aplica correctamente (no hay self-invocation).
 *
 * La fase AFTER_COMMIT garantiza que los emails solo se envían cuando
 * la transacción principal fue confirmada en la base de datos.
 */
@Component
@RequiredArgsConstructor
public class CancelacionBloqueoEventListener {

    private static final Logger log = LoggerFactory.getLogger(CancelacionBloqueoEventListener.class);

    private final ServicioNotificacionBloqueo servicioNotificacionBloqueo;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCancelacionPorBloqueo(CancelacionBloqueoEvent event) {
        log.info("🔔 Bloqueo confirmado en DB — disparando {} notificación(es) de cancelación (hilo: {})",
                event.getDatos().size(), Thread.currentThread().getName());
        servicioNotificacionBloqueo.notificarCancelacionPorBloqueo(event.getDatos());
    }
}
