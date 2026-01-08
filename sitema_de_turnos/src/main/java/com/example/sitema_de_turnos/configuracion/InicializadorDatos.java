package com.example.sitema_de_turnos.configuracion;

import com.example.sitema_de_turnos.modelo.SuperAdmin;
import com.example.sitema_de_turnos.repositorio.RepositorioSuperAdmin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InicializadorDatos implements CommandLineRunner {

    private final RepositorioSuperAdmin repositorioSuperAdmin;
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
        // Verificar si ya existe algún SuperAdmin
        long cantidadSuperAdmins = repositorioSuperAdmin.count();

        if (cantidadSuperAdmins >= 3) {
            log.warn("⚠️ Ya existen {} SuperAdmins. Máximo permitido: 3", cantidadSuperAdmins);
            return;
        }

        if (cantidadSuperAdmins == 0) {
            log.info("No se encontró ningún SuperAdmin. Creando usuario por defecto...");

            SuperAdmin superAdmin = new SuperAdmin();
            superAdmin.setNombre("Admin");
            superAdmin.setApellido("Sistema");
            superAdmin.setEmail(adminDefaultEmail);
            superAdmin.setContrasena(passwordEncoder.encode(adminDefaultPassword));
            superAdmin.setTelefono("0000000000");
            superAdmin.setActivo(true);

            // El rol se asigna automáticamente en @PrePersist
            repositorioSuperAdmin.save(superAdmin);

            log.info("✅ SuperAdmin creado exitosamente");
            log.info("   Email: {}", adminDefaultEmail);
            
            // Warning de seguridad si se usa contraseña por defecto
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
