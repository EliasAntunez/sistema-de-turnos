package com.example.sitema_de_turnos.servicio.notificacion;

import com.example.sitema_de_turnos.dto.notificacion.ReminderData;
import com.example.sitema_de_turnos.modelo.Cliente;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.Pago;
import com.example.sitema_de_turnos.modelo.PerfilProfesional;
import com.example.sitema_de_turnos.modelo.Servicio;
import com.example.sitema_de_turnos.modelo.Turno;
import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.repositorio.RepositorioPago;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests exhaustivos para EmailNotificationService
 * Cubre: validaciones, construcción de HTML, envío, manejo de errores
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailNotificationService Tests")
class EmailNotificationServiceTest {

    @Mock
    private RepositorioPago repositorioPago;

    @Mock
    private TemplateEngine templateEngine;

    private EmailNotificationService emailService;

    private ReminderData reminderDataValido;

    @BeforeEach
    void setUp() {
        emailService = new EmailNotificationService(repositorioPago, templateEngine);
        
        ReflectionTestUtils.setField(emailService, "enabled", true);
        ReflectionTestUtils.setField(emailService, "brevoApiKey", "test-api-key");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:5173");

        reminderDataValido = ReminderData.builder()
                .turnoId(1L)
                .clienteEmail("cliente@example.com")
                .clienteNombre("Juan Pérez")
                .fecha(LocalDate.now().plusDays(1))
                .horaInicio(LocalTime.of(10, 0))
                .horaFin(LocalTime.of(10, 30))
                .fechaHoraCompleta(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(10, 0)))
                .servicioNombre("Consulta General")
                .profesionalNombre("Dr. López")
                .empresaNombre("Clínica Test")
                .empresaTelefono("+5491112345678")
                .empresaDireccion("Av. Principal 123")
                .empresaCiudad("Buenos Aires")
                .empresaProvincia("Buenos Aires")
                .build();

        lenient().when(repositorioPago.findByTurnoId(anyLong())).thenReturn(Optional.empty());
        lenient().when(templateEngine.process(anyString(), any())).thenReturn("<html>ok</html>");
    }

    @Test
    @DisplayName("isAvailable debe retornar true cuando está habilitado y con API key")
    void isAvailableDebeRetornarTrueCuandoEstaHabilitadoConApiKey() {
        assertTrue(emailService.isAvailable());
    }

    @Test
    @DisplayName("isAvailable debe retornar false cuando está deshabilitado")
    void isAvailableDebeRetornarFalseCuandoEstaDeshabilitado() {
        ReflectionTestUtils.setField(emailService, "enabled", false);
        
        assertFalse(emailService.isAvailable());
    }

    @Test
    @DisplayName("isAvailable debe retornar false cuando falta API key")
    void isAvailableDebeRetornarFalseCuandoFaltaApiKey() {
        ReflectionTestUtils.setField(emailService, "brevoApiKey", "   ");

        assertFalse(emailService.isAvailable());
    }

    @Test
    @DisplayName("getNotificationType debe retornar EMAIL")
    void getNotificationTypeDebeRetornarEmail() {
        assertEquals("EMAIL", emailService.getNotificationType());
    }

    @Test
    @DisplayName("sendReminder no debe propagar excepción con datos nulos")
    void sendReminderNoDebePropagarExcepcionConDatosNulos() {
        assertDoesNotThrow(() -> emailService.sendReminder(null));
    }

    @Test
    @DisplayName("sendReminder no debe propagar excepción con datos válidos")
    void sendReminderNoDebePropagarExcepcionConDatosValidos() {
        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    @Test
    @DisplayName("enviarCorreoConfirmacionTurno no debe propagar excepción")
    void enviarCorreoConfirmacionTurnoNoDebePropagarExcepcion() {
        Turno turno = construirTurnoValido();
        Pago pago = new Pago();
        pago.setMonto(new BigDecimal("2500.00"));
        when(repositorioPago.findByTurnoId(turno.getId())).thenReturn(Optional.of(pago));

        assertDoesNotThrow(() -> emailService.enviarCorreoConfirmacionTurno(turno));
    }

    private Turno construirTurnoValido() {
        Usuario usuarioProfesional = new Usuario();
        usuarioProfesional.setNombre("Ana");
        usuarioProfesional.setApellido("García");

        PerfilProfesional profesional = new PerfilProfesional();
        profesional.setUsuario(usuarioProfesional);

        Empresa empresa = new Empresa();
        empresa.setNombre("Centro Integral Test");

        Servicio servicio = new Servicio();
        servicio.setNombre("Masoterapia");

        Cliente cliente = new Cliente();
        cliente.setNombre("Juan Pérez");
        cliente.setEmail("cliente@example.com");

        Turno turno = new Turno();
        turno.setId(99L);
        turno.setEmpresa(empresa);
        turno.setServicio(servicio);
        turno.setProfesional(profesional);
        turno.setCliente(cliente);
        turno.setFecha(LocalDate.now().plusDays(1));
        turno.setHoraInicio(LocalTime.of(9, 30));
        turno.setPrecio(new BigDecimal("10000.00"));
        return turno;
    }
}
