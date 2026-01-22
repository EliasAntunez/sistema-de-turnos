package com.example.sitema_de_turnos;

import com.example.sitema_de_turnos.modelo.*;
import com.example.sitema_de_turnos.repositorio.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReservaIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RepositorioEmpresa repoEmpresa;

    @Autowired
    private RepositorioUsuario repoUsuario;

    @Autowired
    private RepositorioServicio repoServicio;

    @Autowired
    private RepositorioProfesional repoProfesional;

    @Autowired
    private RepositorioCliente repoCliente;

    @Autowired
    private RepositorioTurno repoTurno;

    @Autowired
    private EntityManager entityManager;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Empresa empresaA;
    private Servicio servicioA;
    private Profesional profesionalA;

    @BeforeEach
    @org.springframework.transaction.annotation.Transactional
    public void setup() {
        repoTurno.deleteAll();
        repoCliente.deleteAll();
        repoProfesional.deleteAll();
        repoServicio.deleteAll();
        repoEmpresa.deleteAll();
        repoUsuario.deleteAll();
        // Clear persistence context to avoid managed entities referencing transient ones
        entityManager.clear();

        empresaA = new Empresa();
        empresaA.setNombre("Empresa A");
        empresaA.setSlug("empresa-a");
        empresaA.setActiva(true);
        empresaA.setBufferPorDefecto(10);
        empresaA.setCuit("20304050600");
        // Crear y asociar un Dueno requerido por la entidad Empresa
        Dueno dueno = new Dueno();
        dueno.setNombre("Dueno");
        dueno.setApellido("Propietario");
        dueno.setEmail("dueno@example.com");
        dueno.setTelefono("+5491122233344");
        dueno.setContrasena("secret");
        dueno.setActivo(true);
        // Persistir primero el Dueno sin referenciar la Empresa
        repoUsuario.save(dueno);
        Usuario usuarioRecargado = repoUsuario.findById(dueno.getId()).orElseThrow();
        dueno = (Dueno) usuarioRecargado;
        // Asociar el Dueno a la Empresa y persistir la Empresa
        empresaA.setDueno(dueno);
        repoEmpresa.save(empresaA);

        servicioA = new Servicio();
        servicioA.setNombre("Corte");
        servicioA.setDuracionMinutos(30);
        servicioA.setActivo(true);
        servicioA.setEmpresa(empresaA);
        servicioA.setPrecio(java.math.BigDecimal.valueOf(100.0));
        repoServicio.save(servicioA);

        profesionalA = new Profesional();
        profesionalA.setNombre("Yeye");
        profesionalA.setApellido("Apellido");
        profesionalA.setEmpresa(empresaA);
        profesionalA.setActivo(true);
        profesionalA.setEmail("yeye@example.com");
        profesionalA.setTelefono("+5491119998888");
        profesionalA.setContrasena("secret");
        repoProfesional.save(profesionalA);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

            mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }
    @Test
    public void crearTurno_conTelefonoDeCuentaRegistrada_debeRetornar409() throws Exception {
        // Crear cliente con tieneUsuario = true
        Cliente cliente = new Cliente();
        cliente.setEmpresa(empresaA);
        cliente.setNombre("Cuenta Registrada");
        cliente.setTelefono("+5491112345678");
        cliente.setEmail("cliente@example.com");
        cliente.setTieneUsuario(true);
        cliente.setActivo(true);
        repoCliente.save(cliente);

        var request = new java.util.HashMap<String,Object>();
        request.put("servicioId", servicioA.getId());
        request.put("profesionalId", profesionalA.getId());
        request.put("fecha", LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        request.put("horaInicio", "09:00");
        request.put("nombreCliente", "Invitado");
        request.put("telefonoCliente", "+5491112345678");

        mockMvc.perform(post("/api/publico/empresa/" + empresaA.getSlug() + "/turnos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El teléfono pertenece a una cuenta registrada. Por favor, iniciá sesión o usá otro número."));
    }

    @Test
    public void crearTurno_conTelefonoDeClienteInvitado_debeRetornar201() throws Exception {
        // Crear cliente invitado tieneUsuario = false
        Cliente cliente = new Cliente();
        cliente.setEmpresa(empresaA);
        cliente.setNombre("Invitado");
        cliente.setTelefono("+5491110000000");
        cliente.setTieneUsuario(false);
        cliente.setActivo(true);
        repoCliente.save(cliente);

        var request = new java.util.HashMap<String,Object>();
        request.put("servicioId", servicioA.getId());
        request.put("profesionalId", profesionalA.getId());
        request.put("fecha", LocalDate.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE));
        request.put("horaInicio", "10:00");
        request.put("nombreCliente", "Invitado");
        request.put("telefonoCliente", "+5491110000000");

        mockMvc.perform(post("/api/publico/empresa/" + empresaA.getSlug() + "/turnos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exito").value(true))
                .andExpect(jsonPath("$.datos.id").isNumber());
    }
}
