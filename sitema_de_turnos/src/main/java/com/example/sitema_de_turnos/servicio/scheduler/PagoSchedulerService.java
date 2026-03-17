package com.example.sitema_de_turnos.servicio.scheduler;

import com.example.sitema_de_turnos.modelo.EstadoPago;
import com.example.sitema_de_turnos.modelo.EstadoTurno;
import com.example.sitema_de_turnos.modelo.Pago;
import com.example.sitema_de_turnos.modelo.Turno;
import com.example.sitema_de_turnos.repositorio.RepositorioPago;
import com.example.sitema_de_turnos.repositorio.RepositorioTurno;
import com.example.sitema_de_turnos.servicio.notificacion.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoSchedulerService {

    private static final String MOTIVO_CANCELACION_EXPIRACION = "Expirado automáticamente por falta de pago de la seña.";

    private final RepositorioTurno repositorioTurno;
    private final RepositorioPago repositorioPago;
    private final EmailNotificationService emailNotificationService;
    private final TransactionTemplate transactionTemplate;

    @Value("${app.turnos.expiration.grace-minutes}")
    private int graceMinutes;

    @Scheduled(cron = "${app.turnos.expiration.cron}")
    public void expirarPagosPendientes() {
        LocalDateTime ahoraUtc = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime limiteCreacion = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(graceMinutes);

        List<Turno> turnosExpirados = repositorioTurno
            .findTurnosPendientesPagoExpirados(
                EstadoTurno.PENDIENTE_PAGO,
                limiteCreacion
            );

        List<Turno> turnosLiberadosPersistidos = actualizarEstadosExpirados(turnosExpirados, ahoraUtc);

        for (Turno turno : turnosLiberadosPersistidos) {
            try {
                emailNotificationService.enviarCorreoTurnoExpirado(turno);
            } catch (Exception e) {
                log.error("❌ Error enviando correo de turno expirado para turno {}: {}", turno.getId(), e.getMessage(), e);
            }
        }

        log.info("✅ Limpieza completada: {} turnos expirados liberados.", turnosLiberadosPersistidos.size());
    }

    protected List<Turno> actualizarEstadosExpirados(List<Turno> turnosExpirados, LocalDateTime ahoraUtc) {
        return transactionTemplate.execute(status -> {
            if (turnosExpirados == null || turnosExpirados.isEmpty()) {
                return List.of();
            }

            List<Long> turnoIds = turnosExpirados.stream().map(Turno::getId).toList();
            Map<Long, Pago> pagosPorTurno = repositorioPago.findByTurnoIds(turnoIds)
                .stream()
                .collect(Collectors.toMap(pago -> pago.getTurno().getId(), Function.identity()));

            List<Turno> turnosActualizados = new ArrayList<>();
            List<Pago> pagosActualizados = new ArrayList<>();

            for (Turno turno : turnosExpirados) {
                try {
                    turno.setEstado(EstadoTurno.CANCELADO);
                    turno.setMotivoCancelacion(MOTIVO_CANCELACION_EXPIRACION);
                    turno.setCanceladoPor("SISTEMA");
                    turno.setFechaCancelacion(ahoraUtc);
                    turnosActualizados.add(turno);

                    Pago pago = pagosPorTurno.get(turno.getId());
                    if (pago != null && pago.getEstado() == EstadoPago.PENDIENTE) {
                        pago.setEstado(EstadoPago.RECHAZADO);
                        pagosActualizados.add(pago);
                    }
                } catch (Exception e) {
                    log.error("❌ Error preparando expiración para turno {}: {}", turno.getId(), e.getMessage(), e);
                }
            }

            if (!turnosActualizados.isEmpty()) {
                repositorioTurno.saveAll(turnosActualizados);
            }
            if (!pagosActualizados.isEmpty()) {
                repositorioPago.saveAll(pagosActualizados);
            }

            return turnosActualizados;
        });
    }
}
