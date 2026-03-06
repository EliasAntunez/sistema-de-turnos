package com.example.sitema_de_turnos;

import com.example.sitema_de_turnos.dto.RegistroServicioRequest;
import com.example.sitema_de_turnos.dto.ToggleServicioRequest;
import com.example.sitema_de_turnos.modelo.*;
import com.example.sitema_de_turnos.repositorio.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProfesionalServicioIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private RepositorioEmpresa repoEmpresa;
    @Autowired
    private RepositorioUsuario repoUsuario;
    @Autowired
    private RepositorioServicio repoServicio;
    @Autowired
    private RepositorioProfesional repoProfesional;
    @Autowired
    private RepositorioProfesionalServicio repoProfesionalServicio;
    @Autowired
    private EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String EMAIL_DUENO = "dueno@test.com";

    /** ID de la empresa creada en @BeforeEach, usable en todos los tests. */
    private Long empresaId;

    // ===== Setup =====

    @BeforeEach
    @org.springframework.transaction.annotation.Transactional
    public void setup() {
        repoProfesionalServicio.deleteAll();
        repoProfesional.deleteAll();
        repoServicio.deleteAll();
        repoEmpresa.deleteAll();
        repoUsuario.deleteAll();
        entityManager.clear();

        Dueno dueno = new Dueno();
        dueno.setNombre("Carlos");
        dueno.setApellido("Test");
        dueno.setEmail(EMAIL_DUENO);
        dueno.setTelefono("+5491122334455");
        dueno.setContrasena("secret");
        dueno.setActivo(true);
        repoUsuario.save(dueno);
        dueno = (Dueno) repoUsuario.findById(dueno.getId()).orElseThrow();

        Empresa empresa = new Empresa();
        empresa.setNombre("Negocio Test");
        empresa.setSlug("negocio-test");
        empresa.setActiva(true);
        empresa.setBufferPorDefecto(10);
        empresa.setCuit("20111222330");
        empresa.setDueno(dueno);
        repoEmpresa.save(empresa);
        empresaId = empresa.getId();

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    // ===== Bloque B: Tests de integración HTTP =====

    /**
     * Empresa con 1 profesional activo → POST /api/dueno/servicios
     * debe crear automáticamente 1 registro en profesional_servicio (activo=true).
     */
    @Test
    public void postServicio_unipersonalActivo_creaRegistroProfesionalServicio() throws Exception {
        Profesional prof = profesionalActivo("ana@test.com", "+5491133445566");
        repoProfesional.save(prof);

        RegistroServicioRequest req = servicioRequest("Servicio Unico", 30, BigDecimal.valueOf(1500));

        mockMvc.perform(post("/api/dueno/servicios")
                .with(user(EMAIL_DUENO).authorities(new SimpleGrantedAuthority("DUENO")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        List<ProfesionalServicio> registros = repoProfesionalServicio.findAll();
        assertEquals(1, registros.size(), "Auto-onboarding debe crear exactamente 1 registro");
        assertTrue(registros.get(0).getActivo(), "El registro de auto-onboarding debe estar activo");
    }

    /**
     * Empresa con 1 profesional inactivo → POST /api/dueno/servicios
     * NO debe crear ningún registro en profesional_servicio.
     */
    @Test
    public void postServicio_unipersonalInactivo_noCreaRegistroProfesionalServicio() throws Exception {
        Profesional prof = profesionalActivo("pedro@test.com", "+5491144556677");
        prof.setActivo(false);
        repoProfesional.save(prof);

        RegistroServicioRequest req = servicioRequest("Servicio Sin Auto", 45, BigDecimal.valueOf(2000));

        mockMvc.perform(post("/api/dueno/servicios")
                .with(user(EMAIL_DUENO).authorities(new SimpleGrantedAuthority("DUENO")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        assertEquals(0, repoProfesionalServicio.findAll().size(),
                "No debe crearse registro para profesional inactivo");
    }

    /**
     * Empresa con 2 profesionales activos → POST /api/dueno/servicios
     * NO debe crear ningún registro (empresa multi-profesional requiere asignación manual).
     */
    @Test
    public void postServicio_multiprofesional_noCreaRegistroProfesionalServicio() throws Exception {
        repoProfesional.save(profesionalActivo("laura@test.com", "+5491155667788"));
        repoProfesional.save(profesionalActivo("jose@test.com",  "+5491166778899"));

        RegistroServicioRequest req = servicioRequest("Servicio Compartido", 60, BigDecimal.valueOf(3000));

        mockMvc.perform(post("/api/dueno/servicios")
                .with(user(EMAIL_DUENO).authorities(new SimpleGrantedAuthority("DUENO")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        assertEquals(0, repoProfesionalServicio.findAll().size(),
                "No debe haber auto-onboarding con 2 profesionales activos");
    }

    /**
     * GET /api/dueno/profesionales/{id}/servicios
     * Devuelve todos los servicios activos de la empresa indicando disponible=true
     * sólo para los que tienen registro en profesional_servicio.
     */
    @Test
    public void getServiciosDeProfesional_retornaTodosConDisponibleCorrecto() throws Exception {
        Empresa emp = repoEmpresa.findById(empresaId).orElseThrow();

        Profesional prof = profesionalActivo("maria@test.com", "+5491177889900");
        prof.setEmpresa(emp);
        repoProfesional.save(prof);

        Servicio srv1 = servicio("Corte", 30, BigDecimal.valueOf(1500), emp);
        Servicio srv2 = servicio("Peinado", 45, BigDecimal.valueOf(2000), emp);
        repoServicio.save(srv1);
        repoServicio.save(srv2);

        // Sólo srv1 habilitado en whitelist
        ProfesionalServicio ps = new ProfesionalServicio();
        ps.setProfesional(repoProfesional.findById(prof.getId()).orElseThrow());
        ps.setServicio(repoServicio.findById(srv1.getId()).orElseThrow());
        ps.setActivo(true);
        repoProfesionalServicio.save(ps);

        mockMvc.perform(get("/api/dueno/profesionales/" + prof.getId() + "/servicios")
                .with(user(EMAIL_DUENO).authorities(new SimpleGrantedAuthority("DUENO")))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exito").value(true))
                // Los 2 servicios activos de la empresa deben aparecer
                .andExpect(jsonPath("$.datos.length()").value(2))
                // Corte → habilitado en whitelist → disponible=true
                .andExpect(jsonPath("$.datos[?(@.nombre == 'Corte')].disponible",
                        Matchers.contains(true)))
                // Peinado → sin registro → disponible=false
                .andExpect(jsonPath("$.datos[?(@.nombre == 'Peinado')].disponible",
                        Matchers.contains(false)));
    }

    /**
     * PATCH /api/dueno/profesionales/{id}/servicios/toggle con disponible=true
     * cuando no existe registro previo → crea el registro en whitelist con activo=true.
     */
    @Test
    public void toggleServicio_habilitar_creaRegistroEnWhitelist() throws Exception {
        Empresa emp = repoEmpresa.findById(empresaId).orElseThrow();

        Profesional prof = profesionalActivo("sofia@test.com", "+5491188990011");
        prof.setEmpresa(emp);
        repoProfesional.save(prof);

        Servicio srv = servicio("Extensiones", 120, BigDecimal.valueOf(5000), emp);
        repoServicio.save(srv);

        ToggleServicioRequest req = new ToggleServicioRequest(srv.getId(), true);

        mockMvc.perform(patch("/api/dueno/profesionales/" + prof.getId() + "/servicios/toggle")
                .with(user(EMAIL_DUENO).authorities(new SimpleGrantedAuthority("DUENO")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exito").value(true));

        List<ProfesionalServicio> registros = repoProfesionalServicio.findAll();
        assertEquals(1, registros.size(), "Toggle habilitar debe crear 1 registro");
        assertTrue(registros.get(0).getActivo(), "El registro creado debe estar activo");
    }

    /**
     * PATCH /api/dueno/profesionales/{id}/servicios/toggle con disponible=false
     * cuando existe registro → elimina el registro de la whitelist.
     */
    @Test
    public void toggleServicio_deshabilitar_eliminaRegistroDeLaWhitelist() throws Exception {
        Empresa emp = repoEmpresa.findById(empresaId).orElseThrow();

        Profesional prof = profesionalActivo("lucia@test.com", "+5491199001122");
        prof.setEmpresa(emp);
        repoProfesional.save(prof);

        Servicio srv = servicio("Barba", 20, BigDecimal.valueOf(800), emp);
        repoServicio.save(srv);

        ProfesionalServicio ps = new ProfesionalServicio();
        ps.setProfesional(repoProfesional.findById(prof.getId()).orElseThrow());
        ps.setServicio(repoServicio.findById(srv.getId()).orElseThrow());
        ps.setActivo(true);
        repoProfesionalServicio.save(ps);

        assertEquals(1, repoProfesionalServicio.findAll().size(), "Precondición: 1 registro en whitelist");

        ToggleServicioRequest req = new ToggleServicioRequest(srv.getId(), false);

        mockMvc.perform(patch("/api/dueno/profesionales/" + prof.getId() + "/servicios/toggle")
                .with(user(EMAIL_DUENO).authorities(new SimpleGrantedAuthority("DUENO")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exito").value(true));

        assertEquals(0, repoProfesionalServicio.findAll().size(),
                "Toggle deshabilitar debe eliminar el registro de la whitelist");
    }

    // ===== Helpers de construcción =====

    private Profesional profesionalActivo(String email, String telefono) {
        Empresa emp = repoEmpresa.findById(empresaId).orElseThrow();
        Profesional p = new Profesional();
        p.setNombre("Prof");
        p.setApellido("Test");
        p.setEmail(email);
        p.setTelefono(telefono);
        p.setContrasena("secret");
        p.setActivo(true);
        p.setEmpresa(emp);
        return p;
    }

    private RegistroServicioRequest servicioRequest(String nombre, int duracion, BigDecimal precio) {
        RegistroServicioRequest r = new RegistroServicioRequest();
        r.setNombre(nombre);
        r.setDuracionMinutos(duracion);
        r.setPrecio(precio);
        return r;
    }

    private Servicio servicio(String nombre, int duracion, BigDecimal precio, Empresa empresa) {
        Servicio s = new Servicio();
        s.setNombre(nombre);
        s.setDuracionMinutos(duracion);
        s.setPrecio(precio);
        s.setActivo(true);
        s.setEmpresa(empresa);
        return s;
    }
}
