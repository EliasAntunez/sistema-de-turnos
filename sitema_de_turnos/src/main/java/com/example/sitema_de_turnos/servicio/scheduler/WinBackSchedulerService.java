package com.example.sitema_de_turnos.servicio.scheduler;

import com.example.sitema_de_turnos.modelo.BotConfiguracion;
import com.example.sitema_de_turnos.modelo.Cliente;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.EstadoTurno;
import com.example.sitema_de_turnos.repositorio.RepositorioBotConfiguracion;
import com.example.sitema_de_turnos.repositorio.RepositorioCliente;
import com.example.sitema_de_turnos.repositorio.RepositorioEmpresa;
import com.example.sitema_de_turnos.servicio.notificacion.WhatsAppNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class WinBackSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(WinBackSchedulerService.class);
    private static final List<EstadoTurno> ESTADOS_PENDIENTES = List.of(
            EstadoTurno.CONFIRMADO, EstadoTurno.PENDIENTE_PAGO, EstadoTurno.PENDIENTE_CONFIRMACION);

    private final RepositorioEmpresa repositorioEmpresa;
    private final RepositorioCliente repositorioCliente;
    private final RepositorioBotConfiguracion repositorioBotConfiguracion;
    private final WhatsAppNotificationService whatsAppService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public WinBackSchedulerService(
            RepositorioEmpresa repositorioEmpresa,
            RepositorioCliente repositorioCliente,
            RepositorioBotConfiguracion repositorioBotConfiguracion,
            WhatsAppNotificationService whatsAppService) {
        this.repositorioEmpresa = repositorioEmpresa;
        this.repositorioCliente = repositorioCliente;
        this.repositorioBotConfiguracion = repositorioBotConfiguracion;
        this.whatsAppService = whatsAppService;
    }

    @Scheduled(cron = "${app.winback.cron:0 * * * * *}", zone = "${app.scheduler.zone:UTC}")
    public void procesarRecuperacionClientes() {
        log.info("🚀 Iniciando Scheduler Diario de Win-Back...");
        long startTime = System.currentTimeMillis();

        try {
            List<Empresa> empresasActivas = repositorioEmpresa.findByActivaTrue();
            int totalProcesados = 0;

            for (Empresa empresa : empresasActivas) {
                if (Boolean.TRUE.equals(empresa.getWinBackHabilitado())) {
                    try {
                        totalProcesados += procesarTenant(empresa);
                    } catch (Exception e) {
                        log.error("❌ Error al procesar Win-Back para empresa {}: {}", empresa.getId(), e.getMessage());
                    }
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ Scheduler Win-Back finalizado en {}ms. Total de notificaciones a n8n: {}", duration,
                    totalProcesados);
        } catch (Exception e) {
            log.error("❌ Error crítico en scheduler de Win-Back: {}", e.getMessage(), e);
        }
    }

    private int procesarTenant(Empresa empresa) {
        Optional<BotConfiguracion> botConfigOpt = repositorioBotConfiguracion.findByTenantId(empresa.getId());
        if (botConfigOpt.isEmpty()) {
            log.warn(
                    "⏭️ Empresa '{}' (ID: {}) tiene Win-Back activo pero no registra configuración en bot_configuraciones (sin instancia WhatsApp). Saltando.",
                    empresa.getNombre(), empresa.getId());
            return 0;
        }

        String instanciaWhatsapp = botConfigOpt.get().getInstanciaWhatsapp();
        if (instanciaWhatsapp == null || instanciaWhatsapp.isBlank()) {
            log.warn(
                    "⏭️ Empresa '{}' (ID: {}) registra configuración de bot pero la instancia_whatsapp está vacía. Saltando.",
                    empresa.getNombre(), empresa.getId());
            return 0;
        }

        ZoneId timezone = ZoneId
                .of(empresa.getTimezone() != null ? empresa.getTimezone() : "America/Argentina/Buenos_Aires");
        LocalDateTime ahoraLocal = LocalDateTime.now(timezone);

        int diasInactividad = empresa.getWinBackDiasInactividad() != null ? empresa.getWinBackDiasInactividad() : 45;
        LocalDate fechaLimiteInactividad = ahoraLocal.toLocalDate().minusDays(diasInactividad);

        int diasSpam = empresa.getWinBackDiasEsperaReenvio() != null ? empresa.getWinBackDiasEsperaReenvio() : 90;
        LocalDate fechaLimiteSpam = ahoraLocal.toLocalDate().minusDays(diasSpam);

        List<Cliente> clientesElegibles = repositorioCliente.findClientesParaRecuperacion(
                empresa.getId(),
                fechaLimiteInactividad,
                fechaLimiteSpam,
                ahoraLocal.toLocalDate(),
                ahoraLocal.toLocalTime(),
                ESTADOS_PENDIENTES);

        log.info("🔍 Tenant '{}' [Instancia: {}]: {} clientes elegibles para Win-Back.",
                empresa.getNombre(), instanciaWhatsapp, clientesElegibles.size());

        int enviados = 0;
        for (Cliente cliente : clientesElegibles) {
            try {
                enviarMensajeRecuperacion(cliente, empresa, instanciaWhatsapp, ahoraLocal.toLocalDate());
                enviados++;
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("❌ Error al procesar recuperación de cliente {} (ID: {}): {}",
                        cliente.getNombre(), cliente.getId(), e.getMessage());
            }
        }
        return enviados;
    }

    @Transactional
    public void enviarMensajeRecuperacion(Cliente cliente, Empresa empresa, String instanciaWhatsapp,
            LocalDate fechaEnvio) {
        int descuento = empresa.getWinBackDescuentoPorcentaje() != null ? empresa.getWinBackDescuentoPorcentaje() : 10;

        cliente.setFechaUltimoMensajeRecuperacion(fechaEnvio);
        cliente.setWinBackDescuentoPendiente(descuento);
        repositorioCliente.save(cliente);

        String plantilla = empresa.getWinBackMensajePlantilla() != null
                ? empresa.getWinBackMensajePlantilla()
                : "¡Hola {cliente}! Hace mucho no nos visitás. Te extrañamos y por eso queremos regalarte un {descuento}% de descuento en tu próximo turno. Respondé a este mensaje para consultar nuestros horarios y reservar directamente por acá. ¡Te esperamos!";

        String linkReserva = frontendUrl + "/empresa/" + empresa.getSlug();

        String mensajeArmado = plantilla
                .replace("{cliente}", cliente.getNombre())
                .replace("{descuento}", String.valueOf(descuento))
                .replace("{link}", linkReserva);

        whatsAppService.enviarMensajeConInstancia(cliente.getTelefono(), mensajeArmado, instanciaWhatsapp);
    }
}
