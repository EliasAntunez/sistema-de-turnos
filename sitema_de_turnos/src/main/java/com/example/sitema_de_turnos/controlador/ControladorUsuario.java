package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.CambiarContrasenaRequest;
import com.example.sitema_de_turnos.dto.RespuestaApi;
import com.example.sitema_de_turnos.servicio.ServicioCambioContrasena;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN','DUENO','PROFESIONAL')")
public class ControladorUsuario {

    private final ServicioCambioContrasena servicioCambioContrasena;

    @PutMapping("/cambiar-contrasena")
    public ResponseEntity<RespuestaApi<Void>> cambiarContrasena(
            @Valid @RequestBody CambiarContrasenaRequest request,
            Authentication authentication) {

        servicioCambioContrasena.cambiarContrasena(authentication.getName(), request);

        return ResponseEntity.ok(
                RespuestaApi.exitosa(
                        "Contraseña actualizada exitosamente. Inicie sesión nuevamente con su nueva contraseña.",
                        null
                )
        );
    }
}
