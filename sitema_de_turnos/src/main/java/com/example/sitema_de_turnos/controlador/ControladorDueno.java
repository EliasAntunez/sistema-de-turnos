package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.ProfesionalResponse;
import com.example.sitema_de_turnos.dto.RegistroProfesionalRequest;
import com.example.sitema_de_turnos.dto.RespuestaApi;
import com.example.sitema_de_turnos.servicio.ServicioProfesional;
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

    private final ServicioProfesional servicioProfesional;

    @GetMapping("/profesionales")
    public ResponseEntity<List<ProfesionalResponse>> obtenerProfesionales(Authentication authentication) {
        String emailDueno = authentication.getName();
        List<ProfesionalResponse> profesionales = servicioProfesional.obtenerProfesionalesPorEmpresa(emailDueno);
        return ResponseEntity.ok(profesionales);
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
                RespuestaApi.exitosa("Profesional eliminado correctamente", null)
        );
    }
}
