package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.PoliticaCancelacionRequest;
import com.example.sitema_de_turnos.dto.PoliticaCancelacionResponse;
import jakarta.validation.Valid;
import com.example.sitema_de_turnos.servicio.ServicioPoliticaCancelacion;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/politicas-cancelacion")
@RequiredArgsConstructor
public class ControladorPoliticaCancelacion {
    private final ServicioPoliticaCancelacion servicio;
    private final com.example.sitema_de_turnos.servicio.ServicioValidacionDueno servicioValidacionDueno;



    // Ahora los GET usan la empresa del dueño autenticado, no se recibe empresaId
    @GetMapping
    public ResponseEntity<List<PoliticaCancelacionResponse>> listarPorEmpresa() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        com.example.sitema_de_turnos.modelo.Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(email);
        com.example.sitema_de_turnos.modelo.Empresa empresa = dueno.getEmpresa();
        List<PoliticaCancelacionResponse> respuesta = servicio.obtenerPorEmpresaDTO(empresa);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/activas")
    public ResponseEntity<List<PoliticaCancelacionResponse>> listarActivasPorEmpresa() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        com.example.sitema_de_turnos.modelo.Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(email);
        com.example.sitema_de_turnos.modelo.Empresa empresa = dueno.getEmpresa();
        List<PoliticaCancelacionResponse> respuesta = servicio.obtenerActivasPorEmpresaDTO(empresa);
        return ResponseEntity.ok(respuesta);
    }


    @PostMapping
    public ResponseEntity<PoliticaCancelacionResponse> crear(@Valid @RequestBody PoliticaCancelacionRequest request) {
        // Obtener el email del usuario autenticado
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        // Validar y obtener el dueño real y su empresa
        com.example.sitema_de_turnos.modelo.Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(email);
        com.example.sitema_de_turnos.modelo.Empresa empresa = dueno.getEmpresa();
        // Ignorar el empresaId del request, siempre usar la empresa del dueño autenticado
        PoliticaCancelacionResponse respuesta = servicio.crearDesdeDTO(request, empresa);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        servicio.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        servicio.activar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPolitica(@PathVariable Long id, @RequestBody PoliticaCancelacionRequest request) {
        // Obtener la empresa del dueño autenticado
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        com.example.sitema_de_turnos.modelo.Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(email);
        com.example.sitema_de_turnos.modelo.Empresa empresa = dueno.getEmpresa();
        PoliticaCancelacionResponse respuesta = servicio.actualizarDesdeDTO(id, request, empresa);
        return ResponseEntity.ok(respuesta);
    }
}
