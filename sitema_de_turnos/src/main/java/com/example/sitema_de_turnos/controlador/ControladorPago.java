package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.ConfirmarPagoManualRequest;
import com.example.sitema_de_turnos.dto.RespuestaApi;
import com.example.sitema_de_turnos.servicio.ServicioPago;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class ControladorPago {

    private final ServicioPago servicioPago;

    @PostMapping("/{turnoId}/confirmar-manual")
    @PreAuthorize("hasAnyAuthority('DUENO','PROFESIONAL')")
    public ResponseEntity<RespuestaApi<Void>> confirmarPagoManual(
            @PathVariable Long turnoId,
            @Valid @RequestBody ConfirmarPagoManualRequest request,
            Authentication authentication
    ) {
        servicioPago.confirmarPagoManual(authentication.getName(), turnoId, request.getMetodoPago());
        return ResponseEntity.ok(RespuestaApi.exitosa("Pago confirmado y turno actualizado exitosamente", null));
    }
}
