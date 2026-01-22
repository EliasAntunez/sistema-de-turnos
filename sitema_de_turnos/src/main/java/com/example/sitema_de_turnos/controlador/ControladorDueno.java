package com.example.sitema_de_turnos.controlador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.sitema_de_turnos.dto.*;
import com.example.sitema_de_turnos.servicio.ServicioHorarioEmpresa;
import com.example.sitema_de_turnos.servicio.ServicioProfesional;
import com.example.sitema_de_turnos.servicio.ServicioServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dueno")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('DUENO')")
public class ControladorDueno {
    private static final Logger logger = LoggerFactory.getLogger(ControladorDueno.class);

    private final ServicioProfesional servicioProfesional;
    private final ServicioServicio servicioServicio;
    private final ServicioHorarioEmpresa servicioHorarioEmpresa;
    private final com.example.sitema_de_turnos.servicio.ServicioDueno servicioDueno;

    @GetMapping("/profesionales")
    public ResponseEntity<List<ProfesionalResponse>> obtenerProfesionales(Authentication authentication) {
        String emailDueno = authentication.getName();
        List<ProfesionalResponse> profesionales = servicioProfesional.obtenerProfesionalesPorEmpresa(emailDueno);
        return ResponseEntity.ok(profesionales);
    }

    @GetMapping("/empresa")
    public ResponseEntity<?> obtenerEmpresaDelDueno(org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        logger.info("[EMPRESA] Email recibido del usuario autenticado: {}", email);
        com.example.sitema_de_turnos.modelo.Dueno dueno = null;
        try {
            dueno = servicioDueno.obtenerPorEmail(email);
            logger.info("[EMPRESA] Dueño encontrado: {} (id: {})", dueno != null ? dueno.getEmail() : null, dueno != null ? dueno.getId() : null);
        } catch (Exception e) {
            logger.error("[EMPRESA] Error al buscar dueño por email: {} - {}", email, e.getMessage());
            return ResponseEntity.status(500).body("Error al buscar dueño: " + e.getMessage());
        }
        if (dueno == null) {
            logger.warn("[EMPRESA] No se encontró dueño para el email: {}", email);
            return ResponseEntity.status(404).body("No se encontró dueño para el email: " + email);
        }
        if (dueno.getEmpresa() == null) {
            logger.warn("[EMPRESA] El dueño con email {} no tiene empresa asociada", email);
            return ResponseEntity.status(404).body("El dueño no tiene empresa asociada");
        }
        var empresa = dueno.getEmpresa();
        logger.info("[EMPRESA] Empresa encontrada para el dueño {}: id {}", email, empresa.getId());
        // Usar DTO plano para evitar referencias cíclicas
        com.example.sitema_de_turnos.dto.EmpresaDto dto = new com.example.sitema_de_turnos.dto.EmpresaDto(empresa.getId(), empresa.getNombre());
        logger.info("[EMPRESA] DTO a retornar: {}", dto);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/profesionales")
    public ResponseEntity<ProfesionalResponse> crearProfesional(
            @Valid @RequestBody RegistroProfesionalRequest dto,
            Authentication authentication) {
        String emailDueno = authentication.getName();
        ProfesionalResponse profesional = servicioProfesional.crearProfesional(emailDueno, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(profesional);
    }

    @PutMapping("/profesionales/{id}")
    public ResponseEntity<ProfesionalResponse> actualizarProfesional(
            @PathVariable Long id,
            @Valid @RequestBody RegistroProfesionalRequest dto,
            Authentication authentication) {
        String emailDueno = authentication.getName();
        ProfesionalResponse profesional = servicioProfesional.actualizarProfesional(emailDueno, id, dto);
        return ResponseEntity.ok(profesional);
    }

    @DeleteMapping("/profesionales/{id}")
    public ResponseEntity<RespuestaApi<Void>> eliminarProfesional(
            @PathVariable Long id,
            Authentication authentication) {
        String emailDueno = authentication.getName();
        servicioProfesional.eliminarProfesional(emailDueno, id);
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Profesional desactivado correctamente", null)
        );
    }

    @PatchMapping("/profesionales/{id}/activar")
    public ResponseEntity<RespuestaApi<Void>> activarProfesional(
            @PathVariable Long id,
            Authentication authentication) {
        String emailDueno = authentication.getName();
        servicioProfesional.activarProfesional(emailDueno, id);
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Profesional activado correctamente", null)
        );
    }

    @PatchMapping("/profesionales/{id}/desactivar")
    public ResponseEntity<RespuestaApi<Void>> desactivarProfesional(
            @PathVariable Long id,
            Authentication authentication) {
        String emailDueno = authentication.getName();
        servicioProfesional.desactivarProfesional(emailDueno, id);
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Profesional desactivado correctamente", null)
        );
    }

    // ==================== ENDPOINTS PARA SERVICIOS ====================

    @GetMapping("/servicios")
    public ResponseEntity<List<com.example.sitema_de_turnos.dto.ServicioResponse>> obtenerServicios(
            Authentication authentication) {
        String emailDueno = authentication.getName();
        List<com.example.sitema_de_turnos.dto.ServicioResponse> servicios = 
                servicioServicio.obtenerServiciosPorEmpresa(emailDueno);
        return ResponseEntity.ok(servicios);
    }

    @PostMapping("/servicios")
    public ResponseEntity<com.example.sitema_de_turnos.dto.ServicioResponse> crearServicio(
            @Valid @RequestBody RegistroServicioRequest dto,
            Authentication authentication) {
        String emailDueno = authentication.getName();
        com.example.sitema_de_turnos.dto.ServicioResponse servicio = 
                servicioServicio.crearServicio(emailDueno, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio);
    }

    @PutMapping("/servicios/{id}")
    public ResponseEntity<com.example.sitema_de_turnos.dto.ServicioResponse> actualizarServicio(
            @PathVariable Long id,
            @Valid @RequestBody RegistroServicioRequest dto,
            Authentication authentication) {
        String emailDueno = authentication.getName();
        com.example.sitema_de_turnos.dto.ServicioResponse servicio = 
                servicioServicio.actualizarServicio(emailDueno, id, dto);
        return ResponseEntity.ok(servicio);
    }

    @PatchMapping("/servicios/{id}/activar")
    public ResponseEntity<RespuestaApi<Void>> activarServicio(
            @PathVariable Long id,
            Authentication authentication) {
        String emailDueno = authentication.getName();
        servicioServicio.activarServicio(emailDueno, id);
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Servicio activado correctamente", null)
        );
    }

    @PatchMapping("/servicios/{id}/desactivar")
    public ResponseEntity<RespuestaApi<Void>> desactivarServicio(
            @PathVariable Long id,
            Authentication authentication) {
        String emailDueno = authentication.getName();
        servicioServicio.desactivarServicio(emailDueno, id);
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Servicio desactivado correctamente", null)
        );
    }

    /**
     * Obtiene todos los servicios disponibles para un profesional (por especialidad)
     * Indica cuáles están activos y cuáles desactivados
     */
    @GetMapping("/profesionales/{id}/servicios")
    public ResponseEntity<RespuestaApi<List<ProfesionalServicioResponse>>> obtenerServiciosDeProfesional(
            @PathVariable Long id,
            Authentication authentication) {
        String emailDueno = authentication.getName();
        List<ProfesionalServicioResponse> servicios = servicioProfesional.obtenerServiciosDeProfesional(emailDueno, id);
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Servicios del profesional obtenidos exitosamente", servicios)
        );
    }

    /**
     * Activa o desactiva un servicio específico para un profesional
     */
    @PatchMapping("/profesionales/{id}/servicios/toggle")
    public ResponseEntity<RespuestaApi<Void>> toggleServicioProfesional(
            @PathVariable Long id,
            @Valid @RequestBody ToggleServicioRequest request,
            Authentication authentication) {
        String emailDueno = authentication.getName();
        servicioProfesional.toggleServicioProfesional(
                emailDueno, 
                id, 
                request.getServicioId(), 
                request.getDisponible()
        );
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Servicio actualizado correctamente", null)
        );
    }

    // ==================== ENDPOINTS PARA HORARIOS DE EMPRESA ====================

    @PostMapping("/horarios-empresa")
    public ResponseEntity<HorarioEmpresaResponse> crearHorarioEmpresa(
            @Valid @RequestBody HorarioEmpresaRequest dto,
            Authentication authentication) {
        String emailDueno = authentication.getName();
        HorarioEmpresaResponse horario = servicioHorarioEmpresa.crearHorario(emailDueno, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(horario);
    }

    @GetMapping("/horarios-empresa")
    public ResponseEntity<List<HorarioEmpresaResponse>> obtenerHorariosEmpresa(Authentication authentication) {
        String emailDueno = authentication.getName();
        List<HorarioEmpresaResponse> horarios = servicioHorarioEmpresa.obtenerHorariosPorEmpresa(emailDueno);
        return ResponseEntity.ok(horarios);
    }

    @PutMapping("/horarios-empresa/{id}")
    public ResponseEntity<HorarioEmpresaResponse> actualizarHorarioEmpresa(
            @PathVariable Long id,
            @Valid @RequestBody HorarioEmpresaRequest dto,
            Authentication authentication) {
        String emailDueno = authentication.getName();
        HorarioEmpresaResponse horario = servicioHorarioEmpresa.actualizarHorario(emailDueno, id, dto);
        return ResponseEntity.ok(horario);
    }

    @DeleteMapping("/horarios-empresa/{id}")
    public ResponseEntity<RespuestaApi<Void>> eliminarHorarioEmpresa(
            @PathVariable Long id,
            Authentication authentication) {
        String emailDueno = authentication.getName();
        servicioHorarioEmpresa.eliminarHorario(emailDueno, id);
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Horario eliminado correctamente", null)
        );
    }

    @PostMapping("/horarios-empresa/copiar")
    public ResponseEntity<RespuestaApi<String>> copiarHorariosAOtrosDias(
            @Valid @RequestBody com.example.sitema_de_turnos.dto.CopiarHorarioRequest request,
            Authentication authentication) {
        String emailDueno = authentication.getName();
        int horariosCreados = servicioHorarioEmpresa.copiarHorariosAOtrosDias(
                emailDueno, 
                request.getDiaFuente(), 
                request.getDiasDestino(), 
                request.getReemplazar()
        );
        return ResponseEntity.ok(
                RespuestaApi.exitosa(
                    String.format("Se copiaron %d horarios exitosamente", horariosCreados),
                    String.valueOf(horariosCreados)
                )
        );
    }
}
