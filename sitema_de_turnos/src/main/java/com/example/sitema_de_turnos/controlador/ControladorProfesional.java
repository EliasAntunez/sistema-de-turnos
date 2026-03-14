package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.*;
import com.example.sitema_de_turnos.dto.publico.TurnoResponseProfesional;
import com.example.sitema_de_turnos.excepcion.CuentaDesactivadaException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.modelo.PerfilProfesional;
import com.example.sitema_de_turnos.modelo.RolUsuario;
import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.repositorio.RepositorioPerfilProfesional;
import com.example.sitema_de_turnos.repositorio.RepositorioUsuario;
import com.example.sitema_de_turnos.servicio.ServicioBloqueoFecha;
import com.example.sitema_de_turnos.servicio.ServicioDisponibilidad;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para que los profesionales gestionen su propio perfil y disponibilidad
 * 
 * CORREGIDO: Ahora valida empresa activa y maneja invalidaci\u00f3n de sesi\u00f3n
 */
@RestController
@RequestMapping("/api/profesional")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PROFESIONAL')")
public class ControladorProfesional {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioPerfilProfesional repositorioPerfilProfesional;
    private final PasswordEncoder passwordEncoder;
    private final ServicioDisponibilidad servicioDisponibilidad;
    private final ServicioBloqueoFecha servicioBloqueoFecha;
    private final com.example.sitema_de_turnos.servicio.ServicioHorarioEmpresa servicioHorarioEmpresa;
    private final com.example.sitema_de_turnos.servicio.ServicioTurno servicioTurno;
    private final SessionRegistry sessionRegistry;

    /**
     * Obtener el perfil del profesional autenticado
     * CORREGIDO: Ahora valida que empresa esté activa
     */
    @GetMapping("/perfil")
    public ResponseEntity<RespuestaApi<PerfilUsuarioResponse>> obtenerPerfil(Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // VALIDACIÓN: Verificar empresa activa
        PerfilProfesional perfilProfesional = repositorioPerfilProfesional.findByUsuarioEmail(email).orElse(null);
        if (perfilProfesional != null && perfilProfesional.getEmpresa() != null && !perfilProfesional.getEmpresa().getActiva()) {
            throw new CuentaDesactivadaException("Su empresa ha sido desactivada. Contacte al administrador.");
        }

        PerfilUsuarioResponse perfil = mapearAPerfilResponse(usuario);
        
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Perfil obtenido exitosamente", perfil)
        );
    }

    /**
     * Actualizar el perfil del profesional autenticado
     * CORREGIDO: Ahora valida empresa activa e invalida sesiones al cambiar contraseña
     */
    @PutMapping("/perfil")
    public ResponseEntity<RespuestaApi<PerfilUsuarioResponse>> actualizarPerfil(
            @Valid @RequestBody ActualizarPerfilRequest request,
            Authentication authentication) {
        
        String email = authentication.getName();
        Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // VALIDACIÓN: Verificar empresa activa
        PerfilProfesional perfilProfesional = repositorioPerfilProfesional.findByUsuarioEmail(email).orElse(null);
        if (perfilProfesional != null && perfilProfesional.getEmpresa() != null && !perfilProfesional.getEmpresa().getActiva()) {
            throw new CuentaDesactivadaException("Su empresa ha sido desactivada. Contacte al administrador.");
        }

        boolean cambioContrasena = false;

        // Actualizar solo los campos proporcionados
        if (request.getNombre() != null) {
            usuario.setNombre(request.getNombre());
        }
        if (request.getApellido() != null) {
            usuario.setApellido(request.getApellido());
        }
        if (request.getEmail() != null && !request.getEmail().equals(usuario.getEmail())) {
            // Validar que el nuevo email no exista
            if (repositorioUsuario.existsByEmail(request.getEmail())) {
                throw new RuntimeException("El email ya está en uso");
            }
            usuario.setEmail(request.getEmail());
        }
        if (request.getTelefono() != null) {
            usuario.setTelefono(request.getTelefono());
        }
        if (request.getContrasena() != null && !request.getContrasena().isEmpty()) {
            usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));
            cambioContrasena = true;
        }

        Usuario actualizado = repositorioUsuario.save(usuario);
        
        // SEGURIDAD: Invalidar todas las sesiones si cambió la contraseña
        if (cambioContrasena) {
            invalidarTodasLasSesionesDel(usuario.getEmail());
        }
        
        PerfilUsuarioResponse perfil = mapearAPerfilResponse(actualizado);

        return ResponseEntity.ok(
                RespuestaApi.exitosa(
                    cambioContrasena ? 
                        "Perfil actualizado. Por favor, inicie sesión nuevamente con su nueva contraseña." :
                        "Perfil actualizado exitosamente",
                    perfil
                )
        );
    }

    /**
     * Invalida todas las sesiones activas de un usuario.
     * Llamado cuando se cambia la contraseña para prevenir acceso con credenciales antiguas.
     */
    private void invalidarTodasLasSesionesDel(String email) {
        List<Object> principals = sessionRegistry.getAllPrincipals();
        
        for (Object principal : principals) {
            if (principal instanceof org.springframework.security.core.userdetails.User) {
                org.springframework.security.core.userdetails.User user = 
                    (org.springframework.security.core.userdetails.User) principal;
                
                if (user.getUsername().equals(email)) {
                    List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                    for (SessionInformation session : sessions) {
                        session.expireNow();
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation") // setRol() retenido intencionalmente para backward compat
    private PerfilUsuarioResponse mapearAPerfilResponse(Usuario usuario) {
        PerfilUsuarioResponse response = new PerfilUsuarioResponse();
        response.setId(usuario.getId());
        response.setNombre(usuario.getNombre());
        response.setApellido(usuario.getApellido());
        response.setEmail(usuario.getEmail());
        response.setTelefono(usuario.getTelefono());
        // Usar .name() (ej: "PROFESIONAL") en lugar de getDescripcion() para
        // mantener consistencia con ControladorAuth y el contrato del frontend.
        String rolPrimario = usuario.getRoles().isEmpty() ? "" : usuario.getRoles().iterator().next().name();
        response.setRol(rolPrimario); // backward compat — campo intencionalmente retenido
        response.setRoles(usuario.getRoles().stream()
                .map(RolUsuario::name)
                .collect(java.util.stream.Collectors.toList()));
        response.setActivo(usuario.getActivo());
        
        // Agregar información de la empresa va perfil profesional
        PerfilProfesional pp = repositorioPerfilProfesional.findByUsuarioEmail(usuario.getEmail()).orElse(null);
        if (pp != null && pp.getEmpresa() != null) {
            response.setEmpresaId(pp.getEmpresa().getId());
            response.setEmpresaNombre(pp.getEmpresa().getNombre());
        }
        
        return response;
    }

    // ==================== ENDPOINTS PARA DISPONIBILIDAD ====================

    @GetMapping("/horarios-empresa")
    public ResponseEntity<List<com.example.sitema_de_turnos.dto.HorarioEmpresaResponse>> obtenerHorariosEmpresa(Authentication authentication) {
        String email = authentication.getName();
        PerfilProfesional perfil = repositorioPerfilProfesional.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Perfil profesional no encontrado"));
        
        List<com.example.sitema_de_turnos.dto.HorarioEmpresaResponse> horarios = 
                servicioHorarioEmpresa.obtenerHorariosPorProfesional(perfil);
        
        return ResponseEntity.ok(horarios);
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<DisponibilidadResponse>> obtenerDisponibilidad(Authentication authentication) {
        String emailProfesional = authentication.getName();
        List<DisponibilidadResponse> disponibilidad = 
                servicioDisponibilidad.obtenerDisponibilidadPropia(emailProfesional);
        return ResponseEntity.ok(disponibilidad);
    }

    @PostMapping("/disponibilidad/inicializar-desde-empresa")
    public ResponseEntity<RespuestaApi<String>> inicializarDisponibilidadDesdeEmpresa(Authentication authentication) {
        String emailProfesional = authentication.getName();
        int disponibilidadesCreadas = servicioDisponibilidad.inicializarDesdeEmpresa(emailProfesional);
        return ResponseEntity.ok(
                RespuestaApi.exitosa(
                    String.format("Se crearon %d disponibilidades desde los horarios de la empresa", disponibilidadesCreadas),
                    String.valueOf(disponibilidadesCreadas)
                )
        );
    }

    @PostMapping("/disponibilidad")
    public ResponseEntity<DisponibilidadResponse> crearDisponibilidad(
            @Valid @RequestBody RegistroDisponibilidadRequest dto,
            Authentication authentication) {
        String emailProfesional = authentication.getName();
        DisponibilidadResponse disponibilidad = 
                servicioDisponibilidad.crearDisponibilidad(emailProfesional, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(disponibilidad);
    }

    @PutMapping("/disponibilidad/{id}")
    public ResponseEntity<DisponibilidadResponse> actualizarDisponibilidad(
            @PathVariable Long id,
            @Valid @RequestBody RegistroDisponibilidadRequest dto,
            Authentication authentication) {
        String emailProfesional = authentication.getName();
        DisponibilidadResponse disponibilidad = 
                servicioDisponibilidad.actualizarDisponibilidad(emailProfesional, id, dto);
        return ResponseEntity.ok(disponibilidad);
    }

    @DeleteMapping("/disponibilidad/{id}")
    public ResponseEntity<RespuestaApi<Void>> eliminarDisponibilidad(
            @PathVariable Long id,
            Authentication authentication) {
        String emailProfesional = authentication.getName();
        servicioDisponibilidad.eliminarDisponibilidad(emailProfesional, id);
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Disponibilidad eliminada correctamente", null)
        );
    }

    // ==================== ENDPOINTS PARA BLOQUEOS DE FECHAS ====================

    /**
     * Verificar conflictos antes de crear un bloqueo
     */
    @PostMapping("/bloqueos/verificar-conflictos")
    public ResponseEntity<RespuestaApi<ConflictosBloqueoResponse>> verificarConflictos(
            @Valid @RequestBody BloqueoFechaRequest dto,
            Authentication authentication) {
        String emailProfesional = authentication.getName();
        ConflictosBloqueoResponse conflictos = servicioBloqueoFecha.verificarConflictosConTurnos(emailProfesional, dto);
        return ResponseEntity.ok(RespuestaApi.exitosa("Verificación completada", conflictos));
    }

    /**
     * Crear bloqueo con resolución de conflictos
     */
    @PostMapping("/bloqueos/con-resolucion")
    public ResponseEntity<RespuestaApi<BloqueoFechaResponse>> crearBloqueoConResolucion(
            @Valid @RequestBody ResolucionConflictoRequest dto,
            Authentication authentication) {
        String emailProfesional = authentication.getName();
        BloqueoFechaResponse bloqueo = servicioBloqueoFecha.crearBloqueoConResolucion(emailProfesional, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                RespuestaApi.exitosa("Bloqueo creado exitosamente", bloqueo)
        );
    }

    @PostMapping("/bloqueos")
    public ResponseEntity<BloqueoFechaResponse> crearBloqueo(
            @Valid @RequestBody BloqueoFechaRequest dto,
            Authentication authentication) {
        String emailProfesional = authentication.getName();
        BloqueoFechaResponse bloqueo = servicioBloqueoFecha.crearBloqueo(emailProfesional, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(bloqueo);
    }

    @GetMapping("/bloqueos")
    public ResponseEntity<List<BloqueoFechaResponse>> obtenerBloqueos(Authentication authentication) {
        String emailProfesional = authentication.getName();
        List<BloqueoFechaResponse> bloqueos = servicioBloqueoFecha.obtenerBloqueosPropios(emailProfesional);
        return ResponseEntity.ok(bloqueos);
    }

    @PutMapping("/bloqueos/{id}")
    public ResponseEntity<BloqueoFechaResponse> actualizarBloqueo(
            @PathVariable Long id,
            @Valid @RequestBody BloqueoFechaRequest dto,
            Authentication authentication) {
        String emailProfesional = authentication.getName();
        BloqueoFechaResponse bloqueo = servicioBloqueoFecha.actualizarBloqueo(emailProfesional, id, dto);
        return ResponseEntity.ok(bloqueo);
    }

    @DeleteMapping("/bloqueos/{id}")
    public ResponseEntity<RespuestaApi<Void>> eliminarBloqueo(
            @PathVariable Long id,
            Authentication authentication) {
        String emailProfesional = authentication.getName();
        servicioBloqueoFecha.eliminarBloqueo(emailProfesional, id);
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Bloqueo eliminado correctamente", null)
        );
    }

    // ==================== ENDPOINTS PARA GESTIÓN DE TURNOS ====================

    /**
     * Listar turnos del profesional con filtros opcionales
     * GET /api/profesional/turnos?fecha=2026-01-09&fechaDesde=2026-01-09&fechaHasta=2026-01-15
     */
    @GetMapping("/turnos")
    public ResponseEntity<RespuestaApi<List<TurnoResponseProfesional>>> listarTurnos(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            Authentication authentication) {
        String emailProfesional = authentication.getName();
        List<TurnoResponseProfesional> turnos = 
            servicioTurno.listarTurnosProfesional(emailProfesional, fecha, fechaDesde, fechaHasta);
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Turnos obtenidos exitosamente", turnos)
        );
    }

    @GetMapping("/turnos-sin-resolver/cantidad")
    public ResponseEntity<RespuestaApi<Long>> obtenerCantidadTurnosSinResolver(Authentication authentication) {
        String emailProfesional = authentication.getName();
        long cantidad = servicioTurno.contarTurnosSinResolverProfesional(emailProfesional);
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Cantidad obtenida exitosamente", cantidad)
        );
    }

    /**
     * Cambiar estado de un turno
     * PUT /api/profesional/turnos/{id}/estado
     */
    @PutMapping("/turnos/{id}/estado")
    public ResponseEntity<RespuestaApi<TurnoResponseProfesional>> cambiarEstadoTurno(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoTurnoRequest request,
            Authentication authentication) {
        String emailProfesional = authentication.getName();
        TurnoResponseProfesional turno = 
            servicioTurno.cambiarEstadoTurno(emailProfesional, id, request);
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Estado actualizado exitosamente", turno)
        );
    }

    /**
     * Agregar observaciones a un turno
     * PUT /api/profesional/turnos/{id}/observaciones
     */
    @PutMapping("/turnos/{id}/observaciones")
    public ResponseEntity<RespuestaApi<TurnoResponseProfesional>> agregarObservaciones(
            @PathVariable Long id,
            @Valid @RequestBody AgregarObservacionesRequest request,
            Authentication authentication) {
        String emailProfesional = authentication.getName();
        TurnoResponseProfesional turno = 
            servicioTurno.agregarObservacionesTurno(emailProfesional, id, request);
        return ResponseEntity.ok(
                RespuestaApi.exitosa("Observaciones agregadas exitosamente", turno)
        );
    }
}
