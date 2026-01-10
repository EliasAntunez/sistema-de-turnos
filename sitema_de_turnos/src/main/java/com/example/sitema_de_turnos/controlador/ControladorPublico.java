package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.ApiResponse;
import com.example.sitema_de_turnos.dto.publico.*;
import com.example.sitema_de_turnos.servicio.ServicioPublico;
import com.example.sitema_de_turnos.servicio.ServicioTurno;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/publico")
@RequiredArgsConstructor
public class ControladorPublico {

    private final ServicioPublico servicioPublico;
    private final ServicioTurno servicioTurno;

    /**
     * Obtener información pública de una empresa por slug
     * GET /api/publico/empresa/{slug}
     */
    @GetMapping("/empresa/{slug}")
    public ResponseEntity<ApiResponse<EmpresaPublicaResponse>> obtenerEmpresa(@PathVariable String slug) {
        EmpresaPublicaResponse empresa = servicioPublico.obtenerEmpresaPorSlug(slug);
        return ResponseEntity.ok(ApiResponse.exito(empresa, "Empresa obtenida exitosamente"));
    }

    /**
     * Obtener servicios activos de una empresa
     * GET /api/publico/empresa/{slug}/servicios
     */
    @GetMapping("/empresa/{slug}/servicios")
    public ResponseEntity<ApiResponse<List<ServicioPublicoResponse>>> obtenerServicios(@PathVariable String slug) {
        List<ServicioPublicoResponse> servicios = servicioPublico.obtenerServiciosPorEmpresa(slug);
        return ResponseEntity.ok(ApiResponse.exito(servicios, "Servicios obtenidos exitosamente"));
    }

    /**
     * Obtener profesionales que pueden dar un servicio específico
     * GET /api/publico/empresa/{slug}/profesionales?servicioId={id}
     */
    @GetMapping("/empresa/{slug}/profesionales")
    public ResponseEntity<ApiResponse<List<ProfesionalPublicoResponse>>> obtenerProfesionales(
            @PathVariable String slug,
            @RequestParam Long servicioId
    ) {
        List<ProfesionalPublicoResponse> profesionales = 
                servicioPublico.obtenerProfesionalesPorServicio(slug, servicioId);
        return ResponseEntity.ok(ApiResponse.exito(profesionales, "Profesionales obtenidos exitosamente"));
    }

    /**
     * Obtener slots disponibles para reservar
     * GET /api/publico/disponibilidad?empresaSlug=X&servicioId=Y&profesionalId=Z&fecha=YYYY-MM-DD
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<ApiResponse<List<SlotDisponibleResponse>>> obtenerDisponibilidad(
            @RequestParam String empresaSlug,
            @RequestParam Long servicioId,
            @RequestParam Long profesionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        List<SlotDisponibleResponse> slots = servicioPublico.obtenerSlotsDisponibles(
                empresaSlug, servicioId, profesionalId, fecha
        );
        return ResponseEntity.ok(ApiResponse.exito(slots, "Slots disponibles obtenidos exitosamente"));
    }

    /**
     * Crear un turno (reserva)
     * POST /api/publico/empresa/{empresaSlug}/turnos
     */
    @PostMapping("/empresa/{empresaSlug}/turnos")
    public ResponseEntity<ApiResponse<TurnoResponsePublico>> crearTurno(
            @PathVariable String empresaSlug,
            @Valid @RequestBody CrearTurnoRequest request
    ) {
        TurnoResponsePublico turno = servicioTurno.crearTurnoPublico(empresaSlug, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(turno, "Turno creado exitosamente"));
    }
}
