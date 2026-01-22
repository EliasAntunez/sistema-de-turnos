    package com.example.sitema_de_turnos.controlador;

    import com.example.sitema_de_turnos.modelo.Cliente;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpSession;
    import jakarta.validation.Valid;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContext;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
    import org.springframework.web.bind.annotation.*;

    import java.time.LocalDate;
    import java.util.List;

    import com.example.sitema_de_turnos.servicio.ServicioPublico;
    import com.example.sitema_de_turnos.servicio.ServicioTurno;
    import com.example.sitema_de_turnos.servicio.ServicioAutenticacionCliente;
    import com.example.sitema_de_turnos.servicio.ClienteUserDetails;
    import com.example.sitema_de_turnos.dto.ApiResponse;
    import com.example.sitema_de_turnos.dto.publico.EmpresaPublicaResponse;
    import com.example.sitema_de_turnos.dto.publico.ServicioPublicoResponse;
    import com.example.sitema_de_turnos.dto.publico.ProfesionalPublicoResponse;
    import com.example.sitema_de_turnos.dto.publico.SlotDisponibleResponse;
    import com.example.sitema_de_turnos.dto.publico.TurnoResponsePublico;
    import com.example.sitema_de_turnos.dto.publico.CrearTurnoRequest;
    import com.example.sitema_de_turnos.dto.ClienteAutenticadoResponse;
    import com.example.sitema_de_turnos.dto.publico.RegistroClienteRequest;
    import com.example.sitema_de_turnos.dto.publico.LoginClienteRequest;
    import com.example.sitema_de_turnos.dto.publico.TelefonoInfoResponse;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;

    @RestController
    @RequestMapping("/api/publico")
    @RequiredArgsConstructor
    @Slf4j
public class ControladorPublico {

        /**
         * Obtener políticas de cancelación activas de una empresa pública por slug
         * GET /api/publico/empresa/{slug}/politicas-cancelacion/activas
         */
        @GetMapping("/empresa/{slug}/politicas-cancelacion/activas")
        public ResponseEntity<ApiResponse<java.util.List<com.example.sitema_de_turnos.dto.PoliticaCancelacionResponse>>> obtenerPoliticasActivasPorSlug(@PathVariable String slug) {
            java.util.List<com.example.sitema_de_turnos.dto.PoliticaCancelacionResponse> politicas = servicioPublico.obtenerPoliticasActivasPorEmpresa(slug);
            return ResponseEntity.ok(ApiResponse.exito(politicas, "Políticas activas obtenidas exitosamente"));
        }
    private final ServicioPublico servicioPublico;
    private final ServicioTurno servicioTurno;
    private final ServicioAutenticacionCliente servicioAutenticacionCliente;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/empresa/{slug}")
    public ResponseEntity<ApiResponse<EmpresaPublicaResponse>> obtenerEmpresa(@PathVariable String slug) {
        EmpresaPublicaResponse empresa = servicioPublico.obtenerEmpresaPorSlug(slug);
        return ResponseEntity.ok(ApiResponse.exito(empresa, "Empresa obtenida exitosamente"));
    }

    /**
     * Obtener servicios activos de una empresa
     * GET /api/publico/empresa/{slug}/servicios
     */
    @GetMapping("/empresa/{slug}/servicios")
    public ResponseEntity<ApiResponse<List<ServicioPublicoResponse>>> obtenerServicios(@PathVariable String slug) {
        List<ServicioPublicoResponse> servicios = servicioPublico.obtenerServiciosPorEmpresa(slug);
        return ResponseEntity.ok(ApiResponse.exito(servicios, "Servicios obtenidos exitosamente"));
    }

    /**
     * Obtener profesionales que pueden dar un servicio específico
     * GET /api/publico/empresa/{slug}/profesionales?servicioId={id}
     */
    @GetMapping("/empresa/{slug}/profesionales")
    public ResponseEntity<ApiResponse<List<ProfesionalPublicoResponse>>> obtenerProfesionales(
            @PathVariable String slug,
            @RequestParam Long servicioId
    ) {
        List<ProfesionalPublicoResponse> profesionales = 
                servicioPublico.obtenerProfesionalesPorServicio(slug, servicioId);
        return ResponseEntity.ok(ApiResponse.exito(profesionales, "Profesionales obtenidos exitosamente"));
    }

    /**
     * Obtener slots disponibles para reservar
     * GET /api/publico/disponibilidad?empresaSlug=X&servicioId=Y&profesionalId=Z&fecha=YYYY-MM-DD
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<ApiResponse<List<SlotDisponibleResponse>>> obtenerDisponibilidad(
            @RequestParam String empresaSlug,
            @RequestParam Long servicioId,
            @RequestParam Long profesionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        List<SlotDisponibleResponse> slots = servicioPublico.obtenerSlotsDisponibles(
                empresaSlug, servicioId, profesionalId, fecha
        );
        return ResponseEntity.ok(ApiResponse.exito(slots, "Slots disponibles obtenidos exitosamente"));
    }

    /**
     * Crear un turno (reserva)
     * POST /api/publico/empresa/{empresaSlug}/turnos
     */
    @PostMapping("/empresa/{empresaSlug}/turnos")
    public ResponseEntity<ApiResponse<TurnoResponsePublico>> crearTurno(
            @PathVariable String empresaSlug,
            @AuthenticationPrincipal ClienteUserDetails clienteUserDetails,
            @Valid @RequestBody CrearTurnoRequest request
    ) {
        Cliente clienteAutenticado = clienteUserDetails != null ? clienteUserDetails.getCliente() : null;
        TurnoResponsePublico turno = servicioTurno.crearTurnoPublico(empresaSlug, clienteAutenticado, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(turno, "Turno creado exitosamente"));
    }

    /**
     * Registrar cliente (convertir invitado en usuario con cuenta)
     * POST /api/publico/empresa/{empresaSlug}/registro-cliente
     */
    @PostMapping("/empresa/{empresaSlug}/registro-cliente")
    public ResponseEntity<ApiResponse<ClienteAutenticadoResponse>> registrarCliente(
            @PathVariable String empresaSlug,
            @Valid @RequestBody RegistroClienteRequest request,
            HttpServletRequest httpRequest
    ) {
        ClienteAutenticadoResponse cliente = servicioAutenticacionCliente.registrarCliente(empresaSlug, request);
        
        // Después de registrar exitosamente, crear sesión automáticamente
        try {
            // Construir username con formato: "cliente:{empresaSlug}:{telefono}"
            String username = String.format("cliente:%s:%s", empresaSlug, request.getTelefono());
            
            // Autenticar con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getContrasena())
            );

            // Crear contexto de seguridad y guardar en sesión
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            // Guardar en sesión HTTP
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

            log.info("Cliente registrado y autenticado automáticamente: {} - Empresa: {}", request.getTelefono(), empresaSlug);
        } catch (Exception e) {
            log.warn("Cliente registrado pero falló autenticación automática: {}", e.getMessage());
            // No lanzar error, el registro fue exitoso
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito(cliente, "Registro exitoso"));
    }

    /**
     * Login de cliente
     * POST /api/publico/empresa/{empresaSlug}/login-cliente
     */
    @PostMapping("/empresa/{empresaSlug}/login-cliente")
    public ResponseEntity<ApiResponse<ClienteAutenticadoResponse>> loginCliente(
            @PathVariable String empresaSlug,
            @Valid @RequestBody LoginClienteRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            // Construir username con formato: "cliente:{empresaSlug}:{telefono}"
            String username = String.format("cliente:%s:%s", empresaSlug, request.getTelefono());
            
            // Autenticar con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getContrasena())
            );

            // Crear contexto de seguridad y guardar en sesión
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            // Guardar en sesión HTTP
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

            log.info("Login exitoso - Cliente: {} - Empresa: {}", request.getTelefono(), empresaSlug);

            // Obtener datos del cliente
            var cliente = servicioAutenticacionCliente.obtenerClienteParaAutenticacion(empresaSlug, request.getTelefono());
            ClienteAutenticadoResponse clienteResponse = new ClienteAutenticadoResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getTelefono(),
                cliente.getEmail(),
                cliente.getEmpresa().getId(),
                cliente.getEmpresa().getNombre()
            );

            return ResponseEntity.ok(ApiResponse.exito(clienteResponse, "Login exitoso"));

        } catch (Exception e) {
            log.warn("Error en login de cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Teléfono o contraseña incorrectos"));
        }
    }

    /**
     * Verificar si un teléfono tiene cuenta registrada (detección pasiva)
     * GET /api/publico/empresa/{empresaSlug}/verificar-telefono?telefono=
     */
    @GetMapping("/empresa/{empresaSlug}/verificar-telefono")
    public ResponseEntity<ApiResponse<Boolean>> verificarTelefonoRegistrado(
            @PathVariable String empresaSlug,
            @RequestParam String telefono
    ) {
        boolean tieneUsuario = servicioAutenticacionCliente.verificarTelefonoTieneUsuario(empresaSlug, telefono);
        return ResponseEntity.ok(ApiResponse.exito(tieneUsuario, "Verificación completada"));
    }

    /**
     * Información pública ampliada sobre un teléfono dentro de la empresa.
     * Devuelve si existe un cliente y si además tiene usuario.
     */
    @GetMapping("/empresa/{empresaSlug}/telefono-info")
    public ResponseEntity<ApiResponse<com.example.sitema_de_turnos.dto.publico.TelefonoInfoResponse>> telefonoInfo(
            @PathVariable String empresaSlug,
            @RequestParam String telefono
    ) {
        boolean existe = servicioAutenticacionCliente.verificarTelefonoExiste(empresaSlug, telefono);
        boolean tieneUsuario = servicioAutenticacionCliente.verificarTelefonoTieneUsuario(empresaSlug, telefono);

        String nombreEnmascarado = null;
        if (existe) {
            nombreEnmascarado = servicioAutenticacionCliente.obtenerNombreEnmascarado(empresaSlug, telefono);
        }

        var dto = new TelefonoInfoResponse(existe, tieneUsuario, nombreEnmascarado);
        return ResponseEntity.ok(ApiResponse.exito(dto, "Información de teléfono obtenida"));
    }
}
