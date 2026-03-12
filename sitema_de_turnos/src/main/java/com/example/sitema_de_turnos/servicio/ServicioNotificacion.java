package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.NotificacionResponse;
import com.example.sitema_de_turnos.dto.NotificacionWebSocketDTO;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.modelo.Notificacion;
import com.example.sitema_de_turnos.modelo.PerfilProfesional;
import com.example.sitema_de_turnos.modelo.TipoNotificacion;
import com.example.sitema_de_turnos.repositorio.RepositorioNotificacion;
import com.example.sitema_de_turnos.repositorio.RepositorioPerfilProfesional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar notificaciones en tiempo real
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ServicioNotificacion {

    private final RepositorioNotificacion repositorioNotificacion;
    private final RepositorioPerfilProfesional repositorioPerfilProfesional;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    
    // ✅ B8: Días de retención configurables desde properties
    @Value("${app.notifications.retention-days:90}")
    private int retentionDays;

    /**
     * Enviar notificación a un profesional
     * Persiste en BD y envía por WebSocket
     */
    @Transactional
    public Notificacion enviarNotificacion(
            Long profesionalId,
            TipoNotificacion tipo,
            String titulo,
            String mensaje,
            Object datosAdicionales,
            Long turnoId) {
        
        // Obtener profesional
        PerfilProfesional profesional = repositorioPerfilProfesional.findById(profesionalId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        // Serializar datos adicionales a JSON
        String contenidoJson = null;
        if (datosAdicionales != null) {
            try {
                contenidoJson = objectMapper.writeValueAsString(datosAdicionales);
            } catch (JsonProcessingException e) {
                log.error("Error al serializar datos adicionales de notificación", e);
            }
        }

        // Crear y guardar notificación
        Notificacion notificacion = new Notificacion();
        notificacion.setProfesional(profesional);
        notificacion.setTipo(tipo);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setContenidoJson(contenidoJson);
        notificacion.setTurnoId(turnoId);
        notificacion.setLeida(false);

        notificacion = repositorioNotificacion.save(notificacion);
        log.info("Notificación creada: {} - {} (ID: {})", tipo, titulo, notificacion.getId());

        // Enviar por WebSocket
        enviarPorWebSocket(notificacion);

        return notificacion;
    }

    /**
     * Enviar notificación por WebSocket al canal del profesional
     */
    private void enviarPorWebSocket(Notificacion notificacion) {
        try {
            NotificacionWebSocketDTO dto = convertirAWebSocketDTO(notificacion);

            String destination = "/topic/notifications/" + notificacion.getProfesional().getId();
            messagingTemplate.convertAndSend(destination, dto);
            
            log.debug("Notificación enviada por WebSocket a: {}", destination);
        } catch (Exception e) {
            log.error("Error al enviar notificación por WebSocket", e);
            // No lanzar excepción para no interrumpir el flujo principal
        }
    }

    /**
     * Marcar notificación como leída
     */
    @Transactional
    public void marcarComoLeida(Long notificacionId, String emailProfesional) {
        Notificacion notificacion = repositorioNotificacion.findById(notificacionId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Notificación no encontrada"));

        // Verificar que la notificación pertenece al profesional
        if (!notificacion.getProfesional().getUsuario().getEmail().equals(emailProfesional)) {
            throw new RecursoNoEncontradoException("Notificación no encontrada");
        }

        notificacion.marcarComoLeida();
        repositorioNotificacion.save(notificacion);

        log.debug("Notificación {} marcada como leída", notificacionId);
    }

    /**
     * Marcar todas las notificaciones de un profesional como leídas
     */
    @Transactional
    public void marcarTodasComoLeidas(String emailProfesional) {
        PerfilProfesional profesional = obtenerProfesionalPorEmail(emailProfesional);

        List<Notificacion> noLeidas = repositorioNotificacion.findNoLeidasByProfesional(profesional);
        
        for (Notificacion notificacion : noLeidas) {
            notificacion.marcarComoLeida();
        }
        
        if (!noLeidas.isEmpty()) {
            repositorioNotificacion.saveAll(noLeidas);
            log.info("Marcadas {} notificaciones como leídas para profesional {}", 
                noLeidas.size(), emailProfesional);
        }
    }

    /**
     * Obtener notificaciones no leídas de un profesional
     */
    @Transactional(readOnly = true)
    public List<NotificacionResponse> obtenerNoLeidas(String emailProfesional) {
        PerfilProfesional profesional = obtenerProfesionalPorEmail(emailProfesional);

        List<Notificacion> notificaciones = repositorioNotificacion.findNoLeidasByProfesional(profesional);

        return notificaciones.stream()
            .map(this::mapearAResponse)
            .collect(Collectors.toList());
    }

    /**
     * Contar notificaciones no leídas
     */
    @Transactional(readOnly = true)
    public Long contarNoLeidas(String emailProfesional) {
        PerfilProfesional profesional = obtenerProfesionalPorEmail(emailProfesional);

        return repositorioNotificacion.countByProfesionalAndLeidaFalse(profesional);
    }

    /**
     * Obtener historial de notificaciones con paginación
     */
    @Transactional(readOnly = true)
    public Page<NotificacionResponse> obtenerHistorial(String emailProfesional, int pagina, int tamano) {
        PerfilProfesional profesional = obtenerProfesionalPorEmail(emailProfesional);

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Notificacion> notificaciones = repositorioNotificacion
            .findByProfesionalOrderByFechaCreacionDesc(profesional, pageable);

        return notificaciones.map(this::mapearAResponse);
    }

    /**
     * Obtener las últimas N notificaciones
     */
    @Transactional(readOnly = true)
    public List<NotificacionResponse> obtenerUltimas(String emailProfesional, int limite) {
        PerfilProfesional profesional = obtenerProfesionalPorEmail(emailProfesional);

        Pageable pageable = PageRequest.of(0, limite);
        List<Notificacion> notificaciones = repositorioNotificacion
            .findTopNByProfesional(profesional, pageable);

        return notificaciones.stream()
            .map(this::mapearAResponse)
            .collect(Collectors.toList());
    }

    /**
     * ✅ U1 + B8: Limpieza automática de notificaciones leídas antiguas
     * 
     * Se ejecuta el primer día de cada mes a las 3:00 AM.
     * Elimina notificaciones leídas con más de X días configurables (app.notifications.retention-days)
     * para mantener la base de datos limpia sin afectar notificaciones recientes.
     */
    @Scheduled(cron = "0 0 3 1 * *")  // Día 1 de cada mes a las 3:00 AM
    @Transactional
    public void limpiarNotificacionesAntiguas() {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(retentionDays);
        
        log.info("🧹 Iniciando limpieza de notificaciones leídas anteriores a: {}", 
                fechaLimite.atZone(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toString());
        
        try {
            int eliminadas = repositorioNotificacion.deleteNotificacionesLeidasAntiguas(fechaLimite);
            log.info("✅ Limpieza completada - {} notificaciones eliminadas", eliminadas);
        } catch (Exception e) {
            log.error("❌ Error al limpiar notificaciones antiguas", e);
        }
    }

    /**     * ✅ R1: Convertir Notificacion a NotificacionWebSocketDTO
     * Extrae la conversión manual a método reutilizable
     */
    private NotificacionWebSocketDTO convertirAWebSocketDTO(Notificacion notificacion) {
        NotificacionWebSocketDTO dto = new NotificacionWebSocketDTO();
        dto.setId(notificacion.getId());
        dto.setTipo(notificacion.getTipo().name());
        dto.setTitulo(notificacion.getTitulo());
        dto.setMensaje(notificacion.getMensaje());
        dto.setContenidoJson(notificacion.getContenidoJson());
        dto.setTurnoId(notificacion.getTurnoId());
        
        // ✅ M5: Convertir a UTC ISO 8601 (formato: 2026-02-28T15:30:00Z)
        dto.setFechaCreacion(notificacion.getFechaCreacion()
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toString());
        
        return dto;
    }

    /**
     * ✅ R2: Obtener profesional por email con validación
     * Extrae la lógica duplicada de findByEmail().orElseThrow()
     */
    private PerfilProfesional obtenerProfesionalPorEmail(String email) {
        return repositorioPerfilProfesional.findByUsuarioEmail(email)
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));
    }

    /**
     * Mapear entidad a DTO
     */
    private NotificacionResponse mapearAResponse(Notificacion notificacion) {
        NotificacionResponse response = new NotificacionResponse();
        response.setId(notificacion.getId());
        response.setTipo(notificacion.getTipo());
        response.setTitulo(notificacion.getTitulo());
        response.setMensaje(notificacion.getMensaje());
        response.setContenidoJson(notificacion.getContenidoJson());
        response.setTurnoId(notificacion.getTurnoId());
        response.setLeida(notificacion.getLeida());
        
        // ✅ M5: Convertir a UTC ISO 8601
        response.setFechaCreacion(notificacion.getFechaCreacion()
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toString());
        
        if (notificacion.getFechaLectura() != null) {
            response.setFechaLectura(notificacion.getFechaLectura()
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toString());
        }
        
        return response;
    }
}
