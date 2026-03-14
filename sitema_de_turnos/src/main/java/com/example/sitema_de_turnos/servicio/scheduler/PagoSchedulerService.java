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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoSchedulerService {

    private final RepositorioTurno repositorioTurno;
    private final RepositorioPago repositorioPago;
    private final PagoSchedulerConfig pagoSchedulerConfig;

    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void expirarPagosPendientes() {
        LocalDateTime limiteCreacion = LocalDateTime.now().minusHours(pagoSchedulerConfig.getExpiracionHoras());

        List<Turno> turnosExpirados = repositorioTurno
            .findByEstadoAndFechaCreacionBefore(EstadoTurno.PENDIENTE_PAGO, limiteCreacion);

        if (turnosExpirados.isEmpty()) {
            return;
        }

        List<Long> turnoIds = turnosExpirados.stream().map(Turno::getId).toList();
        Map<Long, Pago> pagosPorTurno = repositorioPago.findByTurnoIds(turnoIds)
            .stream()
            .collect(Collectors.toMap(pago -> pago.getTurno().getId(), Function.identity()));

        for (Turno turno : turnosExpirados) {
            turno.setEstado(EstadoTurno.CANCELADO);
            turno.setMotivoCancelacion("Turno cancelado automáticamente por vencimiento del plazo de pago de seña");
            turno.setCanceladoPor("SISTEMA");
            turno.setFechaCancelacion(LocalDateTime.now());

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
