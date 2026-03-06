package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.EmpresaResponse;
import com.example.sitema_de_turnos.dto.RegistroEmpresaConDuenoRequest;
import com.example.sitema_de_turnos.dto.RespuestaApi;
import com.example.sitema_de_turnos.servicio.ServicioEmpresa;
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
}
