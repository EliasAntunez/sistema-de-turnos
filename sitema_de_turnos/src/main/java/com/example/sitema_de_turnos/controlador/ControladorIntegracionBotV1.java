package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.RespuestaApi;
import com.example.sitema_de_turnos.dto.bot.BotConfigResponseDto;
import com.example.sitema_de_turnos.dto.bot.BotCrearTurnoRequestDto;
import com.example.sitema_de_turnos.dto.bot.BotCrearTurnoResponseDto;
import com.example.sitema_de_turnos.dto.bot.BotServicioResponseDto;
import com.example.sitema_de_turnos.servicio.ServicioIntegracionBot;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ControladorIntegracionBotV1 {

    private final ServicioIntegracionBot servicioIntegracionBot;

    @GetMapping("/bots/config")
    public ResponseEntity<RespuestaApi<BotConfigResponseDto>> obtenerConfigBot(
        @RequestParam("instancia_whatsapp") String instanciaWhatsapp
    ) {
        BotConfigResponseDto config = servicioIntegracionBot.obtenerConfig(instanciaWhatsapp);
        return ResponseEntity.ok(RespuestaApi.exitosa("Configuración del bot obtenida exitosamente", config));
    }

    @GetMapping("/tenants/{tenantId}/servicios")
    public ResponseEntity<RespuestaApi<List<BotServicioResponseDto>>> obtenerServiciosPorTenant(
        @PathVariable Long tenantId
    ) {
        List<BotServicioResponseDto> servicios = servicioIntegracionBot.obtenerServiciosPorTenant(tenantId);
        return ResponseEntity.ok(RespuestaApi.exitosa("Servicios obtenidos exitosamente", servicios));
    }

    @PostMapping("/tenants/{tenantId}/turnos")
    public ResponseEntity<RespuestaApi<BotCrearTurnoResponseDto>> crearTurno(
        @PathVariable Long tenantId,
        @Valid @RequestBody BotCrearTurnoRequestDto request
    ) {
        BotCrearTurnoResponseDto turno = servicioIntegracionBot.crearTurno(tenantId, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RespuestaApi.exitosa("Turno creado exitosamente", turno));
    }
}
