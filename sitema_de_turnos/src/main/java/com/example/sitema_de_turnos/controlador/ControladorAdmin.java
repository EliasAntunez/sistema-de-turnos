package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.EmpresaResponse;
import com.example.sitema_de_turnos.dto.EspecialidadResponse;
import com.example.sitema_de_turnos.dto.RegistroEmpresaConDuenoRequest;
import com.example.sitema_de_turnos.dto.RegistroEspecialidadRequest;
import com.example.sitema_de_turnos.dto.RespuestaApi;
import com.example.sitema_de_turnos.servicio.ServicioEmpresa;
import com.example.sitema_de_turnos.servicio.ServicioEspecialidad;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class ControladorAdmin {

    private final ServicioEmpresa servicioEmpresa;
    private final ServicioEspecialidad servicioEspecialidad;

    /**
     * Crear empresa con dueño en una sola transacción
     * Solo accesible por SuperAdmin
     */
    @PostMapping("/empresas")
    public ResponseEntity<RespuestaApi<EmpresaResponse>> crearEmpresaConDueno(
            @Valid @RequestBody RegistroEmpresaConDuenoRequest request) {
        
        EmpresaResponse empresa = servicioEmpresa.crearEmpresaConDueno(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RespuestaApi.exitosa("Empresa creada exitosamente", empresa));
    }

    /**
     * Obtener todas las empresas
     * Vista principal del SuperAdmin
     */
    @GetMapping("/empresas")
    public ResponseEntity<RespuestaApi<List<EmpresaResponse>>> obtenerTodasLasEmpresas() {
        
        List<EmpresaResponse> empresas = servicioEmpresa.obtenerTodasLasEmpresas();
        
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Empresas obtenidas exitosamente", empresas)
        );
    }

    /**
     * Obtener empresa por ID
     */
    @GetMapping("/empresas/{id}")
    public ResponseEntity<RespuestaApi<EmpresaResponse>> obtenerEmpresaPorId(@PathVariable Long id) {
        
        EmpresaResponse empresa = servicioEmpresa.obtenerEmpresaPorId(id);
        
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Empresa encontrada", empresa)
        );
    }

    /**
     * Activar empresa
     */
    @PutMapping("/empresas/{id}/activar")
    public ResponseEntity<RespuestaApi<Void>> activarEmpresa(@PathVariable Long id) {
        
        servicioEmpresa.cambiarEstadoEmpresa(id, true);
        
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Empresa activada exitosamente", null)
        );
    }

    /**
     * Desactivar empresa
     */
    @PutMapping("/empresas/{id}/desactivar")
    public ResponseEntity<RespuestaApi<Void>> desactivarEmpresa(@PathVariable Long id) {
        
        servicioEmpresa.cambiarEstadoEmpresa(id, false);
        
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Empresa desactivada exitosamente", null)
        );
    }

    // ==================== ENDPOINTS PARA ESPECIALIDADES ====================

    /**
     * Crear especialidad
     * Solo SuperAdmin puede crear especialidades
     */
    @PostMapping("/especialidades")
    public ResponseEntity<RespuestaApi<EspecialidadResponse>> crearEspecialidad(
            @Valid @RequestBody RegistroEspecialidadRequest request) {
        
        EspecialidadResponse especialidad = servicioEspecialidad.crearEspecialidad(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RespuestaApi.exitosa("Especialidad creada exitosamente", especialidad));
    }

    /**
     * Obtener todas las especialidades (activas e inactivas)
     */
    @GetMapping("/especialidades")
    public ResponseEntity<RespuestaApi<List<EspecialidadResponse>>> obtenerTodasLasEspecialidades() {
        
        List<EspecialidadResponse> especialidades = servicioEspecialidad.listarTodasLasEspecialidades();
        
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Especialidades obtenidas exitosamente", especialidades)
        );
    }

    /**
     * Obtener especialidad por ID
     */
    @GetMapping("/especialidades/{id}")
    public ResponseEntity<RespuestaApi<EspecialidadResponse>> obtenerEspecialidadPorId(@PathVariable Long id) {
        
        EspecialidadResponse especialidad = servicioEspecialidad.obtenerEspecialidadPorId(id);
        
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Especialidad encontrada", especialidad)
        );
    }

    /**
     * Actualizar especialidad
     */
    @PutMapping("/especialidades/{id}")
    public ResponseEntity<RespuestaApi<EspecialidadResponse>> actualizarEspecialidad(
            @PathVariable Long id,
            @Valid @RequestBody RegistroEspecialidadRequest request) {
        
        EspecialidadResponse especialidad = servicioEspecialidad.actualizarEspecialidad(id, request);
        
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Especialidad actualizada exitosamente", especialidad)
        );
    }

    /**
     * Activar especialidad
     */
    @PatchMapping("/especialidades/{id}/activar")
    public ResponseEntity<RespuestaApi<Void>> activarEspecialidad(@PathVariable Long id) {
        
        servicioEspecialidad.activarEspecialidad(id);
        
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Especialidad activada exitosamente", null)
        );
    }

    /**
     * Desactivar especialidad
     */
    @PatchMapping("/especialidades/{id}/desactivar")
    public ResponseEntity<RespuestaApi<Void>> desactivarEspecialidad(@PathVariable Long id) {
        
        servicioEspecialidad.desactivarEspecialidad(id);
        
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Especialidad desactivada exitosamente", null)
        );
    }
}
