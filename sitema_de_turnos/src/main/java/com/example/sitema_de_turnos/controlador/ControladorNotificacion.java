package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.NotificacionResponse;
import com.example.sitema_de_turnos.dto.RespuestaApi;
import com.example.sitema_de_turnos.servicio.ServicioNotificacion;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de notificaciones
 * Endpoints para historial, marcar como leídas, etc.
 */
@RestController
@RequestMapping("/api/profesional/notificaciones")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PROFESIONAL')")
public class ControladorNotificacion {

    private final ServicioNotificacion servicioNotificacion;

    /**
     * Obtener notificaciones no leídas del profesional autenticado
     * GET /api/profesional/notificaciones/no-leidas
     */
    @GetMapping("/no-leidas")
    public ResponseEntity<RespuestaApi<List<NotificacionResponse>>> obtenerNoLeidas(
            Authentication authentication) {
        
        String emailProfesional = authentication.getName();
        List<NotificacionResponse> notificaciones = servicioNotificacion.obtenerNoLeidas(emailProfesional);
        
        return ResponseEntity.ok(
            RespuestaApi.exitosa("Notificaciones no leídas obtenidas", notificaciones)
        );
    }

    /**
     * Contar notificaciones no leídas
     * GET /api/profesional/notificaciones/contador
     */
    @GetMapping("/contador")
    public ResponseEntity<RespuestaApi<Long>> contarNoLeidas(Authentication authentication) {
        String emailProfesional = authentication.getName();
        Long contador = servicioNotificacion.contarNoLeidas(emailProfesional);
        
        return ResponseEntity.ok(
            RespuestaApi.exitosa("Contador obtenido", contador)
        );
    }

    /**
     * Obtener las últimas N notificaciones
     * GET /api/profesional/notificaciones/recientes?limite=20
     */
    @GetMapping("/recientes")
    public ResponseEntity<RespuestaApi<List<NotificacionResponse>>> obtenerRecientes(
            @RequestParam(defaultValue = "20") int limite,
            Authentication authentication) {
        
        String emailProfesional = authentication.getName();
        List<NotificacionResponse> notificaciones = servicioNotificacion.obtenerUltimas(emailProfesional, limite);
        
        return ResponseEntity.ok(
            RespuestaApi.exitosa("Notificaciones recientes obtenidas", notificaciones)
        );
    }

    /**
     * Obtener historial completo de notificaciones con paginación
     * GET /api/profesional/notificaciones?pagina=0&tamano=20
     */
    @GetMapping
    public ResponseEntity<Page<NotificacionResponse>> obtenerHistorial(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano,
            Authentication authentication) {
        
        String emailProfesional = authentication.getName();
        Page<NotificacionResponse> notificaciones = 
            servicioNotificacion.obtenerHistorial(emailProfesional, pagina, tamano);
        
        return ResponseEntity.ok(notificaciones);
    }

    /**
     * Marcar una notificación como leída
     * PUT /api/profesional/notificaciones/{id}/leer
     */
    @PutMapping("/{id}/leer")
    public ResponseEntity<RespuestaApi<Void>> marcarComoLeida(
            @PathVariable Long id,
            Authentication authentication) {
        
        String emailProfesional = authentication.getName();
        servicioNotificacion.marcarComoLeida(id, emailProfesional);
        
        return ResponseEntity.ok(
            RespuestaApi.exitosa("Notificación marcada como leída", null)
        );
    }

    /**
     * Marcar todas las notificaciones como leídas
     * PUT /api/profesional/notificaciones/leer-todas
     */
    @PutMapping("/leer-todas")
    public ResponseEntity<RespuestaApi<Void>> marcarTodasComoLeidas(Authentication authentication) {
        String emailProfesional = authentication.getName();
        servicioNotificacion.marcarTodasComoLeidas(emailProfesional);
        
        return ResponseEntity.ok(
            RespuestaApi.exitosa("Todas las notificaciones marcadas como leídas", null)
        );
    }
}
