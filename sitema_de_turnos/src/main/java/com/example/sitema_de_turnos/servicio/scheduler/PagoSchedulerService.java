package com.example.sitema_de_turnos.servicio.scheduler;

import com.example.sitema_de_turnos.modelo.EstadoPago;
import com.example.sitema_de_turnos.modelo.EstadoTurno;
import com.example.sitema_de_turnos.modelo.Pago;
import com.example.sitema_de_turnos.modelo.Turno;
import com.example.sitema_de_turnos.repositorio.RepositorioPago;
import com.example.sitema_de_turnos.repositorio.RepositorioTurno;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoSchedulerService {

    private static final ZoneId ZONA_ARGENTINA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final String MOTIVO_CANCELACION_EXPIRACION = "Sistema: Liberado por falta de pago de seña";

    private final RepositorioTurno repositorioTurno;
    private final RepositorioPago repositorioPago;
    private final PagoSchedulerConfig pagoSchedulerConfig;

    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void expirarPagosPendientes() {
        ZonedDateTime ahoraArgentina = ZonedDateTime.now(ZONA_ARGENTINA);
        LocalDateTime ahoraLocal = ahoraArgentina.toLocalDateTime();
        LocalDate fechaActual = ahoraArgentina.toLocalDate();
        LocalTime horaActual = ahoraArgentina.toLocalTime();
        LocalDateTime limiteCreacion = ahoraLocal.minusHours(pagoSchedulerConfig.getExpiracionHoras());

        List<Turno> turnosExpirados = repositorioTurno
            .findTurnosPendientesPagoExpirados(
                EstadoTurno.PENDIENTE_PAGO,
                limiteCreacion,
                fechaActual,
                horaActual
            );

        if (turnosExpirados.isEmpty()) {
            return;
        }

        List<Long> turnoIds = turnosExpirados.stream().map(Turno::getId).toList();
        Map<Long, Pago> pagosPorTurno = repositorioPago.findByTurnoIds(turnoIds)
            .stream()
            .collect(Collectors.toMap(pago -> pago.getTurno().getId(), Function.identity()));

        for (Turno turno : turnosExpirados) {
            turno.setEstado(EstadoTurno.CANCELADO);
            turno.setMotivoCancelacion(MOTIVO_CANCELACION_EXPIRACION);
            turno.setCanceladoPor("SISTEMA");
            turno.setFechaCancelacion(ahoraLocal);

            Pago pago = pagosPorTurno.get(turno.getId());
            if (pago != null && pago.getEstado() == EstadoPago.PENDIENTE) {
                pago.setEstado(EstadoPago.RECHAZADO);
            }
        }

        repositorioTurno.saveAll(turnosExpirados);
        repositorioPago.saveAll(pagosPorTurno.values());

        log.info("⏰ Scheduler de pagos: {} turnos PENDIENTE_PAGO expirados y cancelados", turnosExpirados.size());
    }
}
