package com.example.sitema_de_turnos.job;

import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.EstadoTurno;
import com.example.sitema_de_turnos.modelo.Turno;
import com.example.sitema_de_turnos.repositorio.RepositorioEmpresa;
import com.example.sitema_de_turnos.repositorio.RepositorioTurno;
import com.example.sitema_de_turnos.servicio.whatsapp.ServicioWhatsApp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Job que procesa recordatorios de turnos diariamente.
 * 
 * FASE 3: Envía recordatorios usando ServicioWhatsApp (mock en dev).
 * 
 * FLUJO DE ESTADOS:
 * - CREADO → (job envía recordatorio) → PENDIENTE_CONFIRMACION
 * - PENDIENTE_CONFIRMACION → (cliente responde "SI") → CONFIRMADO
 * - PENDIENTE_CONFIRMACION → (cliente responde "NO") → CANCELADO
 * 
 * Corre todos los días a las 8:00 AM (hora del servidor).
 * Por cada empresa activa:
 * - Verifica si tiene recordatorios activados (activar_recordatorios = true)
 * - Calcula ventana temporal según horas_anticipacion_recordatorio y timezone
 * - Busca turnos SOLO en estado CREADO en esa ventana temporal
 * - Envía recordatorio usando ServicioWhatsApp (mock en dev, real en prod)
 * - Loguea resultado del envío
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JobRecordatoriosTurnos {

    private final RepositorioEmpresa repositorioEmpresa;
    private final RepositorioTurno repositorioTurno;
    private final ServicioWhatsApp servicioWhatsApp;

    /**
     * Procesa recordatorios diariamente a las 8:00 AM.
     * Cron: 0 minutos, 0 segundos, 8 AM, todos los días.
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void procesarRecordatoriosDiarios() {
        log.info("========== INICIO JOB RECORDATORIOS TURNOS ==========");
        
        try {
            // 1. Obtener todas las empresas activas
            List<Empresa> empresasActivas = repositorioEmpresa.findByActivaTrue();
            
            log.info("Total de empresas activas a procesar: {}", empresasActivas.size());
            
            if (empresasActivas.isEmpty()) {
                log.warn("No hay empresas activas. El job finalizó sin procesar nada.");
                return;
            }
            
            int totalTurnosEncontrados = 0;
            
            // 2. Por cada empresa, buscar turnos del día siguiente
            for (Empresa empresa : empresasActivas) {
                try {
                    int turnosPorEmpresa = procesarEmpresa(empresa);
                    totalTurnosEncontrados += turnosPorEmpresa;
                } catch (Exception e) {
                    log.error("Error al procesar empresa {}: {}", empresa.getNombre(), e.getMessage(), e);
                    // Continuar con la siguiente empresa
                }
            }
            
            log.info("========== FIN JOB RECORDATORIOS TURNOS ==========");
            log.info("Total de turnos para recordar: {}", totalTurnosEncontrados);
            
        } catch (Exception e) {
            log.error("Error crítico en el job de recordatorios: {}", e.getMessage(), e);
        }
    }

    /**
     * Procesa una empresa individual: busca turnos según configuración y los loguea.
     * 
     * @param empresa Empresa a procesar
     * @return Cantidad de turnos encontrados
     */
    private int procesarEmpresa(Empresa empresa) {
        log.debug("Procesando empresa: {} (ID: {})", empresa.getNombre(), empresa.getId());
        
        // 0. Verificar si la empresa tiene recordatorios activados
        if (!empresa.getActivarRecordatorios()) {
            log.debug("Empresa '{}': Recordatorios desactivados (activar_recordatorios = false)", 
                empresa.getNombre());
            return 0;
        }
        
        // 1. Calcular ventana temporal según configuración de la empresa
        ZoneId zoneId = ZoneId.of(empresa.getTimezone());
        ZonedDateTime ahoraEmpresa = ZonedDateTime.now(zoneId);
        
        // Calcular el momento objetivo (ahora + horas_anticipacion)
        int horasAnticipacion = empresa.getHorasAnticipacionRecordatorio();
        ZonedDateTime momentoObjetivo = ahoraEmpresa.plusHours(horasAnticipacion);
        LocalDate fechaObjetivo = momentoObjetivo.toLocalDate();
        
        log.debug("Empresa '{}' - Timezone: {} - Horas anticipación: {}h - Fecha objetivo: {}", 
            empresa.getNombre(), empresa.getTimezone(), horasAnticipacion, fechaObjetivo);
        
        // 2. Buscar turnos SOLO en estado CREADO (no enviados todavía)
        // Los turnos en PENDIENTE_CONFIRMACION ya recibieron recordatorio
        List<EstadoTurno> estadosBuscados = List.of(EstadoTurno.CREADO);
        List<Turno> turnosPendientes = repositorioTurno.findByEmpresaAndFechaAndEstadoInOrderByHoraInicio(
            empresa,
            fechaObjetivo,
            estadosBuscados
        );
        
        if (turnosPendientes.isEmpty()) {
            log.debug("Empresa '{}': No hay turnos CREADOS para {}h antes (fecha: {})", 
                empresa.getNombre(), horasAnticipacion, fechaObjetivo);
            return 0;
        }
        
        // 3. Enviar recordatorios por cada turno
        log.info("✓ Empresa '{}': {} turno(s) para recordar ({}h antes, fecha: {})", 
            empresa.getNombre(), 
            turnosPendientes.size(),
            horasAnticipacion,
            fechaObjetivo);
        
        int exitosos = 0;
        int fallidos = 0;
        
        for (Turno turno : turnosPendientes) {
            try {
                // Enviar recordatorio (mock en dev, real en prod)
                String messageId = servicioWhatsApp.enviarRecordatorioTurno(
                    turno.getCliente(), 
                    turno, 
                    empresa
                );
                
                // Actualizar estado del turno: CREADO → PENDIENTE_CONFIRMACION
                turno.setEstado(EstadoTurno.PENDIENTE_CONFIRMACION);
                turno.setFechaEnvioRecordatorio(LocalDateTime.now());
                turno.setWhatsappMessageId(messageId);
                
                repositorioTurno.save(turno);
                
                log.debug("✓ Recordatorio enviado y estado actualizado - Turno ID: {} | Cliente: {} | MessageID: {}", 
                    turno.getId(),
                    turno.getCliente().getNombre(),
                    messageId);
                
                exitosos++;
                
            } catch (Exception e) {
                log.error("✘ Error al enviar recordatorio - Turno ID: {} | Cliente: {} | Error: {}", 
                    turno.getId(),
                    turno.getCliente().getNombre(),
                    e.getMessage());
                fallidos++;
            }
        }
        
        log.info("Empresa '{}': Enviados: {} | Fallidos: {}", 
            empresa.getNombre(), exitosos, fallidos);
        
        return exitosos;
    }
}
