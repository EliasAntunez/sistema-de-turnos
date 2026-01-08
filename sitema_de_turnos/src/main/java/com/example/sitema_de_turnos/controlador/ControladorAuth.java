package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.LoginRequest;
import com.example.sitema_de_turnos.dto.LoginResponse;
import com.example.sitema_de_turnos.dto.RegistroSuperAdminRequest;
import com.example.sitema_de_turnos.dto.RespuestaApi;
import com.example.sitema_de_turnos.modelo.SuperAdmin;
import com.example.sitema_de_turnos.repositorio.RepositorioSuperAdmin;
import com.example.sitema_de_turnos.repositorio.RepositorioUsuario;
import com.example.sitema_de_turnos.servicio.ServicioAutenticacion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ControladorAuth {

    private final ServicioAutenticacion servicioAutenticacion;
    private final RepositorioSuperAdmin repositorioSuperAdmin;
    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder passwordEncoder;

    /**
     * Endpoint de login
     * Público - no requiere autenticación previa
     */
    @PostMapping("/login")
    public ResponseEntity<RespuestaApi<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse respuesta = servicioAutenticacion.autenticar(request);
            
            return ResponseEntity.ok(
                    RespuestaApi.exitosa("Login exitoso", respuesta)
            );
            
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(401)
                    .body(RespuestaApi.error("Email o contraseña incorrectos"));
        }
    }

    /**
     * Endpoint de prueba para verificar que la API está funcionando
     */
    @GetMapping("/health")
    public ResponseEntity<RespuestaApi<String>> health() {
        return ResponseEntity.ok(
                RespuestaApi.exitosa("API funcionando correctamente", "OK")
        );
    }

    /**
     * ========================================================================
     * ENDPOINT DESACTIVADO POR SEGURIDAD
     * ========================================================================
     * Este endpoint permitía crear SuperAdmins de forma pública.
     * Ha sido desactivado para prevenir creación no autorizada de administradores.
     * 
     * Si necesitas crear un SuperAdmin adicional:
     * 1. Descomenta temporalmente este código
     * 2. Crea el SuperAdmin
     * 3. Vuelve a comentar este endpoint inmediatamente
     * 
     * Nota: Ya existe un InicializadorDatos que crea automáticamente un SuperAdmin
     * al inicio con credenciales por defecto (admin@sistema.com / admin123)
     * ========================================================================
     */
    
    /*
    @PostMapping("/registrar-super-admin")
    public ResponseEntity<RespuestaApi<LoginResponse>> registrarSuperAdmin(
            @Valid @RequestBody RegistroSuperAdminRequest request) {
        
        // Validar que no exista otro usuario con el mismo email
        if (repositorioUsuario.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(RespuestaApi.error("Ya existe un usuario con ese email"));
        }

        // Crear SuperAdmin
        SuperAdmin superAdmin = new SuperAdmin();
        superAdmin.setNombre(request.getNombre());
        superAdmin.setApellido(request.getApellido());
        superAdmin.setEmail(request.getEmail());
        superAdmin.setContrasena(passwordEncoder.encode(request.getContrasena()));
        superAdmin.setTelefono(request.getTelefono());
        superAdmin.setActivo(true);
        
        // El rol se asigna automáticamente en @PrePersist
        superAdmin = repositorioSuperAdmin.save(superAdmin);

        // Retornar datos del usuario creado
        LoginResponse response = new LoginResponse(
                superAdmin.getId(),
                superAdmin.getNombre(),
                superAdmin.getApellido(),
                superAdmin.getEmail(),
                superAdmin.getRol()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RespuestaApi.exitosa("SuperAdmin registrado exitosamente", response));
    }
    */
}
