package com.example.sitema_de_turnos.configuracion;

import com.example.sitema_de_turnos.modelo.RolUsuario;
import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.repositorio.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class InicializadorDatos implements CommandLineRunner {

    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.email}")
    private String adminDefaultEmail;

    @Value("${admin.default.password}")
    private String adminDefaultPassword;

    @Override
    public void run(String... args) throws Exception {
        inicializarSuperAdmin();
    }

    private void inicializarSuperAdmin() {
        long cantidadSuperAdmins = repositorioUsuario.countByRol(RolUsuario.SUPER_ADMIN);

        if (cantidadSuperAdmins >= 3) {
            log.warn("⚠️ Ya existen {} SuperAdmins. Máximo permitido: 3", cantidadSuperAdmins);
            return;
        }

        if (cantidadSuperAdmins == 0) {
            log.info("No se encontró ningún SuperAdmin. Creando usuario por defecto...");

            Usuario superAdmin = new Usuario();
            superAdmin.setNombre("Admin");
            superAdmin.setApellido("Sistema");
            superAdmin.setEmail(adminDefaultEmail);
            superAdmin.setContrasena(passwordEncoder.encode(adminDefaultPassword));
            superAdmin.setTelefono("0000000000");
            superAdmin.setActivo(true);
            superAdmin.setFechaCreacion(LocalDateTime.now());
            superAdmin.setFechaActualizacion(LocalDateTime.now());
            superAdmin.getRoles().add(RolUsuario.SUPER_ADMIN);

            repositorioUsuario.save(superAdmin);

            log.info("✅ SuperAdmin creado exitosamente");
            log.info("   Email: {}", adminDefaultEmail);

            if ("changeMe123!".equals(adminDefaultPassword)) {
                log.warn("⚠️⚠️⚠️ ALERTA DE SEGURIDAD ⚠️⚠️⚠️");
                log.warn("   Usando contraseña por defecto del sistema");
                log.warn("   Configure la variable de entorno ADMIN_DEFAULT_PASSWORD");
                log.warn("   NUNCA use esta configuración en producción");
            }
        } else {
            log.info("✓ SuperAdmin ya existe en la base de datos ({}/3 permitidos)", cantidadSuperAdmins);
        }
    }
}
