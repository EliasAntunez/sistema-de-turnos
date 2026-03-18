package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.RegistroDispositivoTokenRequest;
import com.example.sitema_de_turnos.dto.RespuestaApi;
import com.example.sitema_de_turnos.servicio.ServicioDispositivoToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class ControladorPushNotificacion {

    private final ServicioDispositivoToken servicioDispositivoToken;

    @PostMapping("/token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RespuestaApi<Void>> registrarToken(
            @Valid @RequestBody RegistroDispositivoTokenRequest request,
            Authentication authentication) {

        servicioDispositivoToken.registrarToken(authentication.getName(), request);

        return ResponseEntity.ok(
            RespuestaApi.exitosa("Token de dispositivo registrado correctamente", null)
        );
    }
}