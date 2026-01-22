package com.example.sitema_de_turnos;

import com.example.sitema_de_turnos.dto.publico.CancelarReservaRequest;
import com.example.sitema_de_turnos.dto.publico.ReservaModificarRequest;
import com.example.sitema_de_turnos.modelo.*;
import com.example.sitema_de_turnos.repositorio.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
public class ClienteReservaIntegrationTest {

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
        entityManager.clear();

        empresaA = new Empresa();
        empresaA.setNombre("Empresa A");
        empresaA.setSlug("empresa-a");
        empresaA.setActiva(true);
        empresaA.setBufferPorDefecto(10);
        empresaA.setCuit("20304050600");

        Dueno dueno = new Dueno();
        dueno.setNombre("Dueno");
        dueno.setApellido("Prop");
        dueno.setEmail("dueno@example.com");
        dueno.setTelefono("+5491122233344");
        dueno.setContrasena("secret");
        dueno.setActivo(true);
        repoUsuario.save(dueno);
        Usuario usuarioRecargado = repoUsuario.findById(dueno.getId()).orElseThrow();
        dueno = (Dueno) usuarioRecargado;
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

        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void cancelar_reserva_por_duenio_debeRetornar200() throws Exception {
        // Crear cliente y turno
        Cliente cliente = new Cliente();
        cliente.setEmpresa(empresaA);
        cliente.setNombre("Invitado");
        cliente.setTelefono("+5491110000001");
        cliente.setTieneUsuario(true);
        cliente.setActivo(true);
        repoCliente.save(cliente);

        Turno turno = new Turno();
        turno.setEmpresa(empresaA);
        turno.setServicio(servicioA);
        turno.setProfesional(profesionalA);
        turno.setCliente(cliente);
        turno.setFecha(java.time.LocalDate.now().plusDays(2));
        turno.setHoraInicio(java.time.LocalTime.of(10,0));
        turno.setHoraFin(java.time.LocalTime.of(10,30));
        turno.setDuracionMinutos(30);
        turno.setBufferMinutos(10);
        turno.setPrecio(servicioA.getPrecio());
        turno.setEstado(EstadoTurno.CREADO);
        repoTurno.save(turno);

        var req = new CancelarReservaRequest("Cambio de planes");

        mockMvc.perform(post("/api/cliente/reservas/" + turno.getId() + "/cancelar")
            .with(user("cliente:" + empresaA.getSlug() + ":" + cliente.getTelefono())
                .authorities(new SimpleGrantedAuthority("CLIENTE")))
            .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                ;
    }

    @Test
    public void cancelar_reserva_por_otro_cliente_debeRetornar403() throws Exception {
        // Crear cliente dueño
        Cliente cliente = new Cliente();
        cliente.setEmpresa(empresaA);
        cliente.setNombre("Invitado");
        cliente.setTelefono("+5491110000002");
        cliente.setTieneUsuario(false);
        cliente.setActivo(true);
        repoCliente.save(cliente);

        // Otro cliente (no dueño)
        Cliente otro = new Cliente();
        otro.setEmpresa(empresaA);
        otro.setNombre("Otro");
        otro.setTelefono("+5491110000999");
        otro.setTieneUsuario(true);
        otro.setActivo(true);
        repoCliente.save(otro);

        Turno turno = new Turno();
        turno.setEmpresa(empresaA);
        turno.setServicio(servicioA);
        turno.setProfesional(profesionalA);
        turno.setCliente(cliente);
        turno.setFecha(java.time.LocalDate.now().plusDays(2));
        turno.setHoraInicio(java.time.LocalTime.of(11,0));
        turno.setHoraFin(java.time.LocalTime.of(11,30));
        turno.setDuracionMinutos(30);
        turno.setBufferMinutos(10);
        turno.setPrecio(servicioA.getPrecio());
        turno.setEstado(EstadoTurno.CREADO);
        repoTurno.save(turno);

        var req = new CancelarReservaRequest("No soy dueño");

        mockMvc.perform(post("/api/cliente/reservas/" + turno.getId() + "/cancelar")
            .with(user("cliente:" + empresaA.getSlug() + ":" + otro.getTelefono())
                .authorities(new SimpleGrantedAuthority("CLIENTE")))
            .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                ;
    }

    @Test
    public void modificar_reserva_por_duenio_debeActualizarNombreYObservaciones() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setEmpresa(empresaA);
        cliente.setNombre("Invitado");
        cliente.setTelefono("+5491110000003");
        cliente.setTieneUsuario(true);
        cliente.setActivo(true);
        repoCliente.save(cliente);

        Turno turno = new Turno();
        turno.setEmpresa(empresaA);
        turno.setServicio(servicioA);
        turno.setProfesional(profesionalA);
        turno.setCliente(cliente);
        turno.setFecha(java.time.LocalDate.now().plusDays(3));
        turno.setHoraInicio(java.time.LocalTime.of(12,0));
        turno.setHoraFin(java.time.LocalTime.of(12,30));
        turno.setDuracionMinutos(30);
        turno.setBufferMinutos(10);
        turno.setPrecio(servicioA.getPrecio());
        turno.setEstado(EstadoTurno.CREADO);
        repoTurno.save(turno);

        ReservaModificarRequest req = new ReservaModificarRequest("Nombre Nuevo", "Traigo puntualidad");

        mockMvc.perform(put("/api/cliente/reservas/" + turno.getId())
            .with(user("cliente:" + empresaA.getSlug() + ":" + cliente.getTelefono())
                .authorities(new SimpleGrantedAuthority("CLIENTE")))
            .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.observaciones").value("Traigo puntualidad"));

        var turnoActualizado = repoTurno.findById(turno.getId()).orElseThrow();
        assertEquals("Nombre Nuevo", turnoActualizado.getCliente().getNombre());
    }

    @Test
    public void reprogramar_reserva_por_duenio_debeActualizarFechaYHora() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setEmpresa(empresaA);
        cliente.setNombre("Invitado");
        cliente.setTelefono("+5491110000010");
        cliente.setTieneUsuario(true);
        cliente.setActivo(true);
        repoCliente.save(cliente);

        Turno turno = new Turno();
        turno.setEmpresa(empresaA);
        turno.setServicio(servicioA);
        turno.setProfesional(profesionalA);
        turno.setCliente(cliente);
        turno.setFecha(java.time.LocalDate.now().plusDays(3));
        turno.setHoraInicio(java.time.LocalTime.of(10,0));
        turno.setHoraFin(java.time.LocalTime.of(10,30));
        turno.setDuracionMinutos(30);
        turno.setBufferMinutos(10);
        turno.setPrecio(servicioA.getPrecio());
        turno.setEstado(EstadoTurno.CREADO);
        repoTurno.save(turno);

        // Reprogramar a otro día/hora
        java.util.Map<String, Object> req = new java.util.HashMap<>();
        req.put("fecha", java.time.LocalDate.now().plusDays(4).toString());
        req.put("horaInicio", "14:00");

        mockMvc.perform(put("/api/cliente/reservas/" + turno.getId() + "/reprogramar")
            .with(user("cliente:" + empresaA.getSlug() + ":" + cliente.getTelefono())
                .authorities(new SimpleGrantedAuthority("CLIENTE")))
            .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datos.horaInicio").value("14:00"));

        var turnoActualizado = repoTurno.findById(turno.getId()).orElseThrow();
        assertEquals(java.time.LocalDate.now().plusDays(4), turnoActualizado.getFecha());
        assertEquals(java.time.LocalTime.of(14,0), turnoActualizado.getHoraInicio());
    }

    @Test
    public void reprogramar_reserva_conflicto_debeRetornar400() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setEmpresa(empresaA);
        cliente.setNombre("Invitado");
        cliente.setTelefono("+5491110000020");
        cliente.setTieneUsuario(true);
        cliente.setActivo(true);
        repoCliente.save(cliente);

        // Turno origen
        Turno turno = new Turno();
        turno.setEmpresa(empresaA);
        turno.setServicio(servicioA);
        turno.setProfesional(profesionalA);
        turno.setCliente(cliente);
        turno.setFecha(java.time.LocalDate.now().plusDays(3));
        turno.setHoraInicio(java.time.LocalTime.of(9,0));
        turno.setHoraFin(java.time.LocalTime.of(9,30));
        turno.setDuracionMinutos(30);
        turno.setBufferMinutos(10);
        turno.setPrecio(servicioA.getPrecio());
        turno.setEstado(EstadoTurno.CREADO);
        repoTurno.save(turno);

        // Otro turno que causa conflicto en la nueva fecha/hora
        Turno otro = new Turno();
        otro.setEmpresa(empresaA);
        otro.setServicio(servicioA);
        otro.setProfesional(profesionalA);
        otro.setCliente(cliente);
        otro.setFecha(java.time.LocalDate.now().plusDays(4));
        otro.setHoraInicio(java.time.LocalTime.of(11,0));
        otro.setHoraFin(java.time.LocalTime.of(11,30));
        otro.setDuracionMinutos(30);
        otro.setBufferMinutos(10);
        otro.setPrecio(servicioA.getPrecio());
        otro.setEstado(EstadoTurno.CREADO);
        repoTurno.save(otro);

        // Intentar reprogramar el turno original al horario ocupado por 'otro'
        java.util.Map<String, Object> req = new java.util.HashMap<>();
        req.put("fecha", java.time.LocalDate.now().plusDays(4).toString());
        req.put("horaInicio", "11:00");

        mockMvc.perform(put("/api/cliente/reservas/" + turno.getId() + "/reprogramar")
            .with(user("cliente:" + empresaA.getSlug() + ":" + cliente.getTelefono())
                .authorities(new SimpleGrantedAuthority("CLIENTE")))
            .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
