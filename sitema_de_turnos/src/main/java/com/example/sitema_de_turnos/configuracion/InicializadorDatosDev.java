package com.example.sitema_de_turnos.configuracion;

import com.example.sitema_de_turnos.modelo.*;
import com.example.sitema_de_turnos.repositorio.*;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inicializa datos de prueba solo en el perfil "dev".
 * NO se ejecuta nunca en producción (@Profile("dev")).
 *
 * Crea el dataset mínimo para poder testear el flujo completo:
 *  - 1 Usuario con roles DUENO + PROFESIONAL (mismo usuario, dos perfiles)
 *  - 1 PerfilDueno vinculado
 *  - 1 Empresa vinculada al PerfilDueno
 *  - 1 PerfilProfesional del mismo usuario en la misma empresa
 *  - 2 Servicios en la empresa
 *
 * Credenciales de prueba:
 *  Email:    dueno@test.com
 *  Password: Test1234!
 *  Slug empresa: test-empresa
 */
@Component
@Profile("dev")
@Order(2)  // Se ejecuta DESPUÉS de InicializadorDatos (Order 1 por defecto)
@RequiredArgsConstructor
@Slf4j
public class InicializadorDatosDev implements CommandLineRunner {

    private static final String EMAIL_TEST   = "dueno@test.com";
    private static final String PASSWORD_TEST = "Test1234!";
    private static final String EMPRESA_SLUG  = "test-empresa";
    private static final String EMPRESA_CUIT  = "30-99999999-9";

    private final RepositorioUsuario           repositorioUsuario;
    private final RepositorioPerfilDueno       repositorioPerfilDueno;
    private final RepositorioPerfilProfesional repositorioPerfilProfesional;
    private final RepositorioEmpresa           repositorioEmpresa;
    private final RepositorioServicio          repositorioServicio;
    private final RepositorioProfesionalServicio repositorioProfesionalServicio;
    private final PasswordEncoder              passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (repositorioUsuario.existsByEmail(EMAIL_TEST)) {
            log.info("✓ Datos de prueba dev ya existen ({}). Skipping.", EMAIL_TEST);
            return;
        }

        log.info("🔧 Creando datos de prueba para perfil DEV...");

        // 1. Usuario con doble rol (dueño + profesional)
        Usuario usuario = new Usuario();
        usuario.setNombre("Juan");
        usuario.setApellido("Pérez");
        usuario.setEmail(EMAIL_TEST);
        usuario.setContrasena(passwordEncoder.encode(PASSWORD_TEST));
        usuario.setTelefono("3764000000");
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuario.getRoles().add(RolUsuario.DUENO);
        usuario.getRoles().add(RolUsuario.PROFESIONAL);
        usuario = repositorioUsuario.save(usuario);
        log.info("  ✅ Usuario creado: {}", EMAIL_TEST);

        // 2. PerfilDueno
        PerfilDueno perfilDueno = new PerfilDueno();
        perfilDueno.setUsuario(usuario);
        perfilDueno.setDocumento("30999999");
        perfilDueno.setTipoDocumento("DNI");
        perfilDueno.setActivo(true);
        perfilDueno = repositorioPerfilDueno.save(perfilDueno);
        log.info("  ✅ PerfilDueno creado");

        // 3. Empresa (referencia al PerfilDueno como owner)
        Empresa empresa = new Empresa();
        empresa.setNombre("Peluquería Test");
        empresa.setSlug(EMPRESA_SLUG);
        empresa.setCuit(EMPRESA_CUIT);
        empresa.setDescripcion("Empresa de prueba para smoke tests");
        empresa.setDireccion("Av. Siempreviva 742");
        empresa.setCiudad("Posadas");
        empresa.setProvincia("Misiones");
        empresa.setTelefono("3764111222");
        empresa.setEmail("contacto@test.com");
        empresa.setBufferPorDefecto(10);
        empresa.setTiempoMinimoAnticipacionMinutos(30);
        empresa.setDiasMaximosReserva(30);
        empresa.setTimezone("America/Argentina/Buenos_Aires");
        empresa.setActiva(true);
        empresa.setPerfilDueno(perfilDueno);
        empresa = repositorioEmpresa.save(empresa);
        log.info("  ✅ Empresa creada: slug={}", EMPRESA_SLUG);

        // 4. PerfilProfesional (mismo usuario, misma empresa — dueño-profesional)
        PerfilProfesional perfilProfesional = new PerfilProfesional();
        perfilProfesional.setUsuario(usuario);
        perfilProfesional.setEmpresa(empresa);
        perfilProfesional.setDescripcion("Peluquero principal y dueño del local");
        perfilProfesional.setActivo(true);
        perfilProfesional = repositorioPerfilProfesional.save(perfilProfesional);
        log.info("  ✅ PerfilProfesional creado (id={})", perfilProfesional.getId());

        // 5. Servicios
        Servicio corte = new Servicio();
        corte.setNombre("Corte de cabello");
        corte.setDescripcion("Corte clásico para caballero");
        corte.setDuracionMinutos(30);
        corte.setBufferMinutos(10);
        corte.setPrecio(new BigDecimal("2500.00"));
        corte.setActivo(true);
        corte.setEmpresa(empresa);
        repositorioServicio.save(corte);

        Servicio barba = new Servicio();
        barba.setNombre("Arreglo de barba");
        barba.setDescripcion("Delineado y arreglo de barba");
        barba.setDuracionMinutos(20);
        barba.setBufferMinutos(5);
        barba.setPrecio(new BigDecimal("1500.00"));
        barba.setActivo(true);
        barba.setEmpresa(empresa);
        repositorioServicio.save(barba);

        // 6. Asociar profesional con ambos servicios (ProfesionalServicio)
        ProfesionalServicio ps1 = new ProfesionalServicio();
        ps1.setProfesional(perfilProfesional);
        ps1.setServicio(corte);
        ps1.setActivo(true);
        repositorioProfesionalServicio.save(ps1);

        ProfesionalServicio ps2 = new ProfesionalServicio();
        ps2.setProfesional(perfilProfesional);
        ps2.setServicio(barba);
        ps2.setActivo(true);
        repositorioProfesionalServicio.save(ps2);

        log.info("  ✅ Servicios creados: 2 (ambos asociados al profesional)");

        log.info("");
        log.info("══════════════════════════════════════════");
        log.info("  DATOS DE PRUEBA DEV listos:");
        log.info("  Login: {}  /  {}", EMAIL_TEST, PASSWORD_TEST);
        log.info("  Empresa slug: {}", EMPRESA_SLUG);
        log.info("  PerfilProfesional id: {}", perfilProfesional.getId());
        log.info("══════════════════════════════════════════");
    }
}
