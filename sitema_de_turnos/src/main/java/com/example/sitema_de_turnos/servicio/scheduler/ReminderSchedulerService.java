package com.example.sitema_de_turnos.servicio.scheduler;

import com.example.sitema_de_turnos.dto.notificacion.ReminderData;
import com.example.sitema_de_turnos.excepcion.NotificationException;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.EstadoTurno;
import com.example.sitema_de_turnos.modelo.Turno;
import com.example.sitema_de_turnos.repositorio.RepositorioEmpresa;
import com.example.sitema_de_turnos.repositorio.RepositorioTurno;
import com.example.sitema_de_turnos.servicio.notificacion.NotificationService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Scheduler que ejecuta el envío de recordatorios de turnos
 * Se ejecuta según el cron configurado en application.properties
 */
@Service
public class ReminderSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(ReminderSchedulerService.class);
    
    private final RepositorioTurno repositorioTurno;
    private final RepositorioEmpresa repositorioEmpresa;
    private final NotificationService notificationService;
    private final ReminderConfig config;
    
    public ReminderSchedulerService(
            RepositorioTurno repositorioTurno,
            RepositorioEmpresa repositorioEmpresa,
            NotificationService notificationService,
            ReminderConfig config) {
        this.repositorioTurno = repositorioTurno;
        this.repositorioEmpresa = repositorioEmpresa;
        this.notificationService = notificationService;
        this.config = config;
    }
    
    @PostConstruct
    public void init() {
        if (config.isEnabled()) {
            log.info("🚀 Scheduler de recordatorios ACTIVADO");
            log.info("📧 Tipo de notificación: {}", notificationService.getNotificationType());
            log.info("⏰ Recordatorios se enviarán {} horas antes del turno", config.getHoursBefore());
            log.info("🔁 Máximo de reintentos: {}", config.getMaxRetries());
        } else {
            log.warn("⚠️ Scheduler de recordatorios DESHABILITADO");
        }
    }
    
    /**
     * Ejecuta el proceso de recordatorios según el cron configurado.
     * Por defecto: cada minuto (0 * * * * *) para no perder ninguna ventana horaria.
     */
    @Scheduled(cron = "${app.reminder.cron:0 * * * * *}", zone = "${app.scheduler.zone:UTC}")
    public void procesarRecordatorios() {
        // 🔍 LOG DE LATIDO: primer log, siempre visible, confirma que el cron dispara
        log.info("⏰ Ejecutando Scheduler de Recordatorios...");

        if (!config.isEnabled()) {
            log.info("⏰ Scheduler DESHABILITADO (app.reminder.enabled=false) - saltando");
            return;
        }
        
        if (!notificationService.isAvailable()) {
            log.error("❌ Servicio de notificación no disponible - Abortando ejecución");
            return;
        }
        
        log.info("⏰ Iniciando proceso de recordatorios...");
        long startTime = System.currentTimeMillis();
        
        try {
            // Procesar cada empresa activa (multi-tenant)
            List<Empresa> empresasActivas = repositorioEmpresa.findByActivaTrue();
            
            log.info("📊 Procesando {} empresas activas", empresasActivas.size());
            
            int totalEnviados = 0;
            int totalErrores = 0;
            
            for (Empresa empresa : empresasActivas) {
                try {
                    int[] resultados = procesarEmpresa(empresa);
                    totalEnviados += resultados[0];
                    totalErrores += resultados[1];
                } catch (Exception e) {
                    log.error("❌ Error al procesar empresa {}: {}", empresa.getId(), e.getMessage());
                    totalErrores++;
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ Proceso completado en {}ms - Enviados: {} | Errores: {}", 
                    duration, totalEnviados, totalErrores);
                log.info("✅ Scheduler de recordatorios finalizado: {} recordatorios enviados en total.", totalEnviados);
            
        } catch (Exception e) {
            log.error("❌ Error crítico en scheduler de recordatorios: {}", e.getMessage(), e);
        }
    }
    
    private int[] procesarEmpresa(Empresa empresa) {
        int enviados = 0;
        int errores = 0;
        
        try {
            // Verificar si la empresa tiene recordatorios habilitados
            if (empresa.getEnviarRecordatorios() == null || !empresa.getEnviarRecordatorios()) {
                log.debug("⏭️ Empresa '{}' tiene recordatorios deshabilitados - Saltando", empresa.getNombre());
                return new int[]{0, 0};
            }
            
            // Obtener horas antes de recordatorio de la empresa (con fallback a config global)
            Integer horasAntesEmpresa = empresa.getHorasAntesRecordatorio();
            int horasAntes = (horasAntesEmpresa != null && horasAntesEmpresa > 0) 
                    ? horasAntesEmpresa 
                    : config.getHoursBefore();
            
            // Calcular ventana de tiempo usando timezone de la empresa
            ZoneId timezone = ZoneId.of(empresa.getTimezone());
            LocalDateTime ahoraLocal = LocalDateTime.now(timezone);
            log.debug("Empresa ID {} ({}): Hora local calculada {}", empresa.getId(), timezone.getId(), ahoraLocal);
            
            // ✅ A3: Ventana de recordatorios con límites inferior y superior
            // Lógica: ahora + minMinutos <= turno <= ahora + horasAntes
            // 
            // - Límite INFERIOR: No enviar si el turno es muy cercano (menos de X minutos)
            //   Ejemplo: Si minMinutesBefore=30, un turno en 20 minutos NO recibe recordatorio
            // 
            // - Límite SUPERIOR: No enviar si el turno es muy lejano (más de X horas)
            //   Ejemplo: Si hoursBefore=24, un turno en 48 horas NO recibe recordatorio
            LocalDateTime fechaHoraMin = ahoraLocal.plusMinutes(config.getMinMinutesBefore()); // No muy cercano
            LocalDateTime fechaHoraMax = ahoraLocal.plusHours(horasAntes); // No muy lejano
            
            // ✅ A2: Descomponer fecha/hora para query JPQL portable
            LocalDate fechaMin = fechaHoraMin.toLocalDate();
            LocalTime horaMin = fechaHoraMin.toLocalTime();
            LocalDate fechaMax = fechaHoraMax.toLocalDate();
            LocalTime horaMax = fechaHoraMax.toLocalTime();
            
            // Buscar turnos pendientes de recordatorio (SOLO CONFIRMADOS)
            List<Turno> turnosPendientes = repositorioTurno.findTurnosPendientesRecordatorio(
                    empresa.getId(), fechaMin, horaMin, fechaMax, horaMax, EstadoTurno.CONFIRMADO);

            // 🔍 LOG SIEMPRE VISIBLE: permite confirmar la consulta y el rango calculado
            log.info("📧 Empresa '{}' (tz={}) - {} turnos pendientes | ventana: {} a {} (+{}min → +{}h)",
                    empresa.getNombre(), timezone.getId(), turnosPendientes.size(),
                    fechaHoraMin, fechaHoraMax,
                    config.getMinMinutesBefore(), horasAntes);
            
            for (Turno turno : turnosPendientes) {
                try {
                    if (procesarTurno(turno)) {
                        enviados++;
                        // Anti-bloqueo: pausa entre envíos para no saturar SMTP de Gmail
                        if (config.getThrottleDelayMs() > 0) {
                            try { Thread.sleep(config.getThrottleDelayMs()); }
                            catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        }
                    } else {
                        errores++;
                    }
                } catch (Exception e) {
                    log.error("❌ Error inesperado al procesar turno {}: {}", turno.getId(), e.getMessage(), e);
                    errores++;
                }
            }
            
        } catch (Exception e) {
            log.error("❌ Error al procesar turnos de empresa {}: {}", empresa.getId(), e.getMessage());
            errores++;
        }
        
        return new int[]{enviados, errores};
    }
    
    /**
     * Procesar un turno para envío de recordatorio.
     * REFACTORIZADO (C3): Thread.sleep() ahora está FUERA de cualquier transacción.
     * Las operaciones de BD se hacen en métodos transaccionales separados y cortos.
     * OPTIMIZADO (M1): Pasa objeto Turno a métodos transaccionales, evitando findById repetidos.
     */
    private boolean procesarTurno(Turno turno) {
        // ✅ A4: Marcar inicio de procesamiento para prevenir duplicados
        marcarInicioProcesamiento(turno);
        
        // Verificar si ya alcanzó el máximo de reintentos (transacción corta)
        if (verificarMaximosReintentosAlcanzados(turno)) {
            return false;
        }
        
        // Construir datos del recordatorio (sin transacción, solo lectura de objetos ya cargados)
        ReminderData reminderData = construirReminderData(turno);
        
        // Intentar enviar con retry exponencial (SIN transacción activa)
        for (int intento = 0; intento <= config.getMaxRetries(); intento++) {
            try {
                // Enviar email (operación externa, sin transacción)
                notificationService.sendReminder(reminderData);
                
                // Éxito - actualizar turno (transacción corta y específica)
                actualizarTurnoExitoso(turno, intento + 1);
                
                // ✅ M6: Nivel debug para logs individuales de turnos (evita spam en producción)
                log.debug("✅ Recordatorio enviado - Turno {} - Cliente: {} (Intento {}/{})", 
                        turno.getId(), turno.getCliente().getEmail(), intento + 1, config.getMaxRetries());
                
                return true;
                
            } catch (NotificationException e) {
                String errorMsg = e.getRootCauseMessage();
                
                // Actualizar intento fallido (transacción corta)
                actualizarIntentoFallido(turno, intento + 1, errorMsg);
                
                // ✅ M4: Usar warn para fallos individuales que se reintentaran, error solo cuando se agoten todos los intentos
                if (intento < config.getMaxRetries()) {
                    log.warn("⚠️ Intento {}/{} falló para turno {} - Error: {} (se reintentará)", 
                            intento + 1, config.getMaxRetries() + 1, turno.getId(), errorMsg);
                } else {
                    log.error("❌ Intento final {}/{} falló para turno {} - Error: {}", 
                            intento + 1, config.getMaxRetries() + 1, turno.getId(), errorMsg);
                }
                
                // ✅ Thread.sleep() FUERA de transacción
                if (intento < config.getMaxRetries()) {
                    long delay = config.getRetryDelayMs() * (long) Math.pow(2, intento);
                    
                    // ✅ M6: Debug en lugar de info para evitar spam en logs
                    log.debug("⏳ Reintentando en {}ms...", delay);
                    
                    try {
                        Thread.sleep(delay); // ✅ AHORA ESTÁ FUERA DE @Transactional
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("❌ Proceso interrumpido para turno {}", turno.getId());
                        
                        // ✅ A4: Resetear marca de procesamiento para permitir reintentos futuros
                        resetearMarcaProcesamiento(turno);
                        return false;
                    }
                }
            } catch (Exception e) {
                log.error("❌ Error inesperado al procesar turno {}: {}", turno.getId(), e.getMessage(), e);
                
                // ✅ A4: Resetear marca de procesamiento para permitir reintentos futuros
                resetearMarcaProcesamiento(turno);
                return false;
            }
        }
        
        // ✅ A4: Todos los intentos fallaron - resetear marca para permitir reintentos en el futuro
        resetearMarcaProcesamiento(turno);
        
        log.warn("⚠️ Turno {} - Todos los intentos fallaron", turno.getId());
        return false;
    }
    
    /**
     * Verificar si el turno ya alcanzó el máximo de reintentos.
     * Transacción corta y específica.
     */
    private boolean verificarMaximosReintentosAlcanzados(Turno turno) {
        int intentos = turno.getRecordatorioIntentos() != null ? turno.getRecordatorioIntentos() : 0;
        
        if (intentos >= config.getMaxRetries()) {
            log.warn("⚠️ Turno {} alcanzó máximo de reintentos ({}) - Marcando como fallido", 
                    turno.getId(), config.getMaxRetries());
            
            turno.setRecordatorioEnviado(true);
            turno.setRecordatorioError("Máximo de reintentos alcanzado");
            repositorioTurno.save(turno);
            return true;
        }
        
        return false;
    }
    
    /**
     * Construir datos del recordatorio sin necesidad de transacción.
     * Los objetos relacionados ya están cargados en memoria.
     */
    private ReminderData construirReminderData(Turno turno) {
        Empresa empresa = turno.getEmpresa();
        LocalDateTime fechaHoraTurno = LocalDateTime.of(turno.getFecha(), turno.getHoraInicio());
        
        return ReminderData.builder()
                .turnoId(turno.getId())
                .clienteEmail(turno.getCliente().getEmail())
                .clienteNombre(turno.getCliente().getNombre())
                .fecha(turno.getFecha())
                .horaInicio(turno.getHoraInicio())
                .horaFin(turno.getHoraFin())
                .fechaHoraCompleta(fechaHoraTurno)
                .servicioNombre(turno.getServicio().getNombre())
                .profesionalNombre(turno.getProfesional().getUsuario().getNombre())
                .empresaNombre(empresa.getNombre())
                .empresaTelefono(empresa.getTelefono())
                .empresaDireccion(empresa.getDireccion())
                .empresaCiudad(empresa.getCiudad())
                .empresaProvincia(empresa.getProvincia())
                .build();
    }
    
    /**
     * Actualizar turno tras envío exitoso.
     * Transacción corta y específica - solo escribe y commitea rápido.
     * OPTIMIZADO (M1): Recibe objeto Turno, evita findById adicional.
     */
    private void actualizarTurnoExitoso(Turno turno, int numeroIntento) {
        turno.setRecordatorioEnviado(true);
        turno.setFechaRecordatorioEnviado(LocalDateTime.now());
        turno.setRecordatorioIntentos(numeroIntento);
        turno.setRecordatorioError(null);
        
        repositorioTurno.save(turno); // save() hace merge automático si está detached
    }
    
    /**
     * Actualizar turno tras intento fallido.
     * Transacción corta y específica - solo escribe y commitea rápido.
     * OPTIMIZADO (M1): Recibe objeto Turno, evita findById adicional.
     */
    private void actualizarIntentoFallido(Turno turno, int numeroIntento, String errorMsg) {
        turno.setRecordatorioIntentos(numeroIntento);
        
        // Truncar mensaje de error si es muy largo
        String errorTruncado = errorMsg.length() > 500 ? errorMsg.substring(0, 500) : errorMsg;
        turno.setRecordatorioError(errorTruncado);
        
        repositorioTurno.save(turno); // save() hace merge automático si está detached
    }
    
    /**
     * ✅ A4: Marcar inicio de procesamiento para prevenir duplicados.
     * 
     * Este timestamp se setea ANTES de empezar a procesar el turno.
     * Mientras esté presente, la query de findTurnosPendientesRecordatorio excluirá este turno,
     * previniendo que múltiples ejecuciones del scheduler procesen el mismo turno simultáneamente.
     * 
     * Transacción corta y específica.
     * OPTIMIZADO (M1): Recibe objeto Turno, evita findById adicional.
     */
    private void marcarInicioProcesamiento(Turno turno) {
        turno.setRecordatorioPrimerIntento(LocalDateTime.now());
        repositorioTurno.save(turno); // save() hace merge automático si está detached
        
        log.debug("🔒 Turno {} marcado como 'en procesamiento'", turno.getId());
    }
    
    /**
     * ✅ A4: Resetear marca de procesamiento cuando todos los intentos fallan.
     * 
     * Esto permite que el scheduler vuelva a intentar enviar el recordatorio en el futuro
     * (útil para errores transitorios de red, SMTP caído, etc.).
     * 
     * Transacción corta y específica.
     * OPTIMIZADO (M1): Recibe objeto Turno, evita findById adicional.
     */
    private void resetearMarcaProcesamiento(Turno turno) {
        turno.setRecordatorioPrimerIntento(null);
        repositorioTurno.save(turno); // save() hace merge automático si está detached
        
        log.debug("🔓 Turno {} liberado para reintentos futuros", turno.getId());
    }
}
