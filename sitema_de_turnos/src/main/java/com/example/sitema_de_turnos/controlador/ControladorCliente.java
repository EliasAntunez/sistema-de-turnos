package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.ApiResponse;
import com.example.sitema_de_turnos.dto.ClienteAutenticadoResponse;
import com.example.sitema_de_turnos.dto.publico.TurnoResponsePublico;
import com.example.sitema_de_turnos.modelo.Cliente;
import com.example.sitema_de_turnos.servicio.ServicioAutenticacionCliente;
import com.example.sitema_de_turnos.servicio.ServicioTurno;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cliente")
@RequiredArgsConstructor
@Slf4j
public class ControladorCliente {

    private final ServicioTurno servicioTurno;
    private final ServicioAutenticacionCliente servicioAutenticacionCliente;
    
    /**
     * Cancelar una reserva del cliente autenticado
     */
    @PostMapping("/reservas/{id}/cancelar")
    public ResponseEntity<ApiResponse<Object>> cancelarReserva(
            @PathVariable Long id,
            @RequestBody com.example.sitema_de_turnos.dto.publico.CancelarReservaRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        String[] parts = username.split(":", 3);
        if (parts.length != 3 || !"cliente".equals(parts[0])) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Sesión inválida"));
        }
        String empresaSlug = parts[1];
        String telefono = parts[2];

        Cliente cliente = servicioAutenticacionCliente.obtenerClienteParaAutenticacion(empresaSlug, telefono);

        servicioTurno.cancelarTurnoPorCliente(id, cliente, request.getMotivo());

        return ResponseEntity.ok(ApiResponse.exito(null, "Turno cancelado exitosamente"));
    }

    /**
     * Modificar reserva del cliente autenticado
     */
    @PutMapping("/reservas/{id}")
    public ResponseEntity<ApiResponse<com.example.sitema_de_turnos.dto.publico.TurnoResponsePublico>> modificarReserva(
            @PathVariable Long id,
            @RequestBody com.example.sitema_de_turnos.dto.publico.ReservaModificarRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        log.info("modificarReserva request - username={}", username);
        String[] parts = username.split(":", 3);
        if (parts.length != 3 || !"cliente".equals(parts[0])) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Sesión inválida"));
        }
        String empresaSlug = parts[1];
        String telefono = parts[2];

        Cliente cliente = servicioAutenticacionCliente.obtenerClienteParaAutenticacion(empresaSlug, telefono);

        var response = servicioTurno.modificarReservaPorCliente(id, cliente, request);
        return ResponseEntity.ok(ApiResponse.exito(response, "Reserva modificada exitosamente"));
    }

    /**
     * Reprogramar una reserva del cliente autenticado
     */
    @PutMapping("/reservas/{id}/reprogramar")
    public ResponseEntity<ApiResponse<com.example.sitema_de_turnos.dto.publico.TurnoResponsePublico>> reprogramarReserva(
            @PathVariable Long id,
            @RequestBody com.example.sitema_de_turnos.dto.publico.ReservaReprogramarRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        String[] parts = username.split(":", 3);
        if (parts.length != 3 || !"cliente".equals(parts[0])) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Sesión inválida"));
        }
        String empresaSlug = parts[1];
        String telefono = parts[2];

        Cliente cliente = servicioAutenticacionCliente.obtenerClienteParaAutenticacion(empresaSlug, telefono);

        var response = servicioTurno.reprogramarReservaPorCliente(id, cliente, request);
        return ResponseEntity.ok(ApiResponse.exito(response, "Reserva reprogramada exitosamente"));
    }

    /**
     * Obtener todos los turnos del cliente autenticado
     * GET /api/cliente/mis-turnos
     */
    @GetMapping("/mis-turnos")
    public ResponseEntity<ApiResponse<List<TurnoResponsePublico>>> obtenerMisTurnos(Authentication authentication) {
        
        // El username tiene formato: "cliente:{empresaSlug}:{telefono}"
        String username = authentication.getName();
        String[] parts = username.split(":", 3);
        
        if (parts.length != 3 || !"cliente".equals(parts[0])) {
            log.error("Formato de username inválido: {}", username);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Sesión inválida"));
        }

        String empresaSlug = parts[1];
        String telefono = parts[2];

        log.info("Obteniendo turnos - Cliente: {} - Empresa: {}", telefono, empresaSlug);

        // Obtener cliente
        Cliente cliente = servicioAutenticacionCliente.obtenerClienteParaAutenticacion(empresaSlug, telefono);
        
        // Obtener todos los turnos del cliente
        List<TurnoResponsePublico> turnos = servicioTurno.obtenerTurnosPorCliente(cliente.getId());

        return ResponseEntity.ok(ApiResponse.exito(turnos, "Turnos obtenidos exitosamente"));
    }

    /**
     * Obtener el perfil del cliente autenticado
     * GET /api/cliente/perfil
     */
    @GetMapping("/perfil")
    public ResponseEntity<ApiResponse<ClienteAutenticadoResponse>> obtenerPerfil(Authentication authentication) {
        
        String username = authentication.getName();
        String[] parts = username.split(":", 3);
        
        if (parts.length != 3 || !"cliente".equals(parts[0])) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Sesión inválida"));
        }

        String empresaSlug = parts[1];
        String telefono = parts[2];

        Cliente cliente = servicioAutenticacionCliente.obtenerClienteParaAutenticacion(empresaSlug, telefono);

        ClienteAutenticadoResponse perfil = new ClienteAutenticadoResponse(
            cliente.getId(),
            cliente.getNombre(),
            cliente.getTelefono(),
            cliente.getEmail(),
            cliente.getEmpresa().getId(),
            cliente.getEmpresa().getNombre()
        );

        return ResponseEntity.ok(ApiResponse.exito(perfil, "Perfil obtenido exitosamente"));
    }
}
