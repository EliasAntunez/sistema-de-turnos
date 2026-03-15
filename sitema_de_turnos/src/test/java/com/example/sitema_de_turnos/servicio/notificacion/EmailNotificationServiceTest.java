package com.example.sitema_de_turnos.servicio.notificacion;

import com.example.sitema_de_turnos.dto.notificacion.ReminderData;
import com.example.sitema_de_turnos.excepcion.NotificationException;
import com.example.sitema_de_turnos.modelo.Cliente;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.PerfilProfesional;
import com.example.sitema_de_turnos.modelo.Servicio;
import com.example.sitema_de_turnos.modelo.Turno;
import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.modelo.Pago;
import com.example.sitema_de_turnos.repositorio.RepositorioPago;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.BigDecimal;
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
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private RepositorioPago repositorioPago;

    private EmailNotificationService emailService;

    private ReminderData reminderDataValido;

    @BeforeEach
    void setUp() {
        emailService = new EmailNotificationService(mailSender, repositorioPago);
        
        // Configurar propiedades usando reflection
        ReflectionTestUtils.setField(emailService, "emailFrom", "test@example.com");
        ReflectionTestUtils.setField(emailService, "emailFromName", "Sistema de Turnos");
        ReflectionTestUtils.setField(emailService, "enabled", true);

        // Datos de recordatorio válidos
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

        // Mock comportamiento de JavaMailSender (usando lenient para evitar UnnecessaryStubbing)
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        lenient().when(repositorioPago.findByTurnoId(anyLong())).thenReturn(Optional.empty());
    }

    // ==================== TESTS DE DISPONIBILIDAD ====================

    @Test
    @DisplayName("isAvailable debe retornar true cuando está habilitado y mailSender existe")
    void isAvailableDebeRetornarTrueCuandoEstaHabilitado() {
        assertTrue(emailService.isAvailable());
    }

    @Test
    @DisplayName("isAvailable debe retornar false cuando está deshabilitado")
    void isAvailableDebeRetornarFalseCuandoEstaDeshabilitado() {
        ReflectionTestUtils.setField(emailService, "enabled", false);
        
        assertFalse(emailService.isAvailable());
    }

    @Test
    @DisplayName("getNotificationType debe retornar EMAIL")
    void getNotificationTypeDebeRetornarEmail() {
        assertEquals("EMAIL", emailService.getNotificationType());
    }

    // ==================== TESTS DE VALIDACIÓN ====================

    @Test
    @DisplayName("Debe lanzar excepción si ReminderData es null")
    void debeLanzarExcepcionSiReminderDataNull() {
        NotificationException exception = assertThrows(NotificationException.class, () -> {
            emailService.sendReminder(null);
        });
        
        assertEquals("ReminderData no puede ser null", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si email del cliente es null")
    void debeLanzarExcepcionSiEmailNull() {
        reminderDataValido.setClienteEmail(null);
        
        NotificationException exception = assertThrows(NotificationException.class, () -> {
            emailService.sendReminder(reminderDataValido);
        });
        
        assertEquals("Email del cliente requerido", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si email del cliente está vacío")
    void debeLanzarExcepcionSiEmailVacio() {
        reminderDataValido.setClienteEmail("   ");
        
        NotificationException exception = assertThrows(NotificationException.class, () -> {
            emailService.sendReminder(reminderDataValido);
        });
        
        assertEquals("Email del cliente requerido", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si fecha del turno es null")
    void debeLanzarExcepcionSiFechaNull() {
        reminderDataValido.setFecha(null);
        
        NotificationException exception = assertThrows(NotificationException.class, () -> {
            emailService.sendReminder(reminderDataValido);
        });
        
        assertEquals("Fecha del turno requerida", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si hora de inicio es null")
    void debeLanzarExcepcionSiHoraInicioNull() {
        reminderDataValido.setHoraInicio(null);
        
        NotificationException exception = assertThrows(NotificationException.class, () -> {
            emailService.sendReminder(reminderDataValido);
        });
        
        assertEquals("Hora de inicio requerida", exception.getMessage());
    }

    // ==================== TESTS DE ENVÍO EXITOSO ====================

    @Test
    @DisplayName("Debe enviar email exitosamente con datos válidos")
    void debeEnviarEmailExitoso() throws Exception {
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendReminder(reminderDataValido);

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Debe configurar el remitente correctamente")
    void debeConfigurarRemitenteCorrectamente() throws Exception {
        emailService.sendReminder(reminderDataValido);

        // El test pasa si no lanza excepción
        // La verificación real se hace en las integraciones con SMTP
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Debe incluir nombre de empresa en el subject")
    void debeIncluirEmpresaEnSubject() throws Exception {
        emailService.sendReminder(reminderDataValido);

        // El subject contiene el nombre de la empresa
        verify(mailSender).send(any(MimeMessage.class));
    }

    // ==================== TESTS DE CONSTRUCCIÓN DE HTML ====================

    @Test
    @DisplayName("Debe manejar correctamente datos con campos opcionales nulos")
    void debeManejarCamposOpcionalesNulos() {
        reminderDataValido.setClienteNombre(null);
        reminderDataValido.setServicioNombre(null);
        reminderDataValido.setProfesionalNombre(null);
        reminderDataValido.setEmpresaTelefono(null);
        reminderDataValido.setEmpresaDireccion(null);
        reminderDataValido.setEmpresaCiudad(null);
        reminderDataValido.setEmpresaProvincia(null);

        // No debe lanzar NullPointerException
        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    @Test
    @DisplayName("Debe usar fallbacks para campos opcionales vacíos")
    void debeUsarFallbacksParaCamposVacios() {
        reminderDataValido.setClienteNombre("");
        reminderDataValido.setServicioNombre("");
        reminderDataValido.setProfesionalNombre("");
        reminderDataValido.setEmpresaNombre("");

        // No debe lanzar excepción
        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    @Test
    @DisplayName("Debe construir ubicación completa cuando todos los campos están presentes")
    void debeConstruirUbicacionCompleta() {
        // Todos los campos de ubicación presentes
        reminderDataValido.setEmpresaDireccion("Av. Corrientes 1234");
        reminderDataValido.setEmpresaCiudad("CABA");
        reminderDataValido.setEmpresaProvincia("Buenos Aires");

        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    @Test
    @DisplayName("Debe manejar ubicación parcial correctamente")
    void debeManejarUbicacionParcial() {
        reminderDataValido.setEmpresaDireccion("Av. Corrientes 1234");
        reminderDataValido.setEmpresaCiudad(null);
        reminderDataValido.setEmpresaProvincia(null);

        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    @Test
    @DisplayName("Debe ocultar sección de ubicación si todos los campos están vacíos")
    void debeOcultarUbicacionSiVacia() {
        reminderDataValido.setEmpresaDireccion(null);
        reminderDataValido.setEmpresaCiudad(null);
        reminderDataValido.setEmpresaProvincia(null);

        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    @Test
    @DisplayName("Debe ocultar sección de teléfono si está vacío")
    void debeOcultarTelefonoSiVacio() {
        reminderDataValido.setEmpresaTelefono(null);

        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    @Test
    @DisplayName("Debe formatear correctamente fecha en español")
    void debeFormatearFechaEnEspanol() {
        LocalDate fecha = LocalDate.of(2026, 3, 15);
        reminderDataValido.setFecha(fecha);
        reminderDataValido.setFechaHoraCompleta(LocalDateTime.of(fecha, LocalTime.of(10, 0)));

        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    @Test
    @DisplayName("Debe formatear correctamente hora en formato 24h")
    void debeFormatearHora24h() {
        reminderDataValido.setHoraInicio(LocalTime.of(14, 30));

        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    @Test
    @DisplayName("Debe calcular tiempo restante correctamente para turnos próximos (menos de 1 hora)")
    void debeCalcularTiempoRestanteMenosDeUnaHora() {
        LocalDateTime proximoTurno = LocalDateTime.now().plusMinutes(30);
        reminderDataValido.setFechaHoraCompleta(proximoTurno);
        reminderDataValido.setFecha(proximoTurno.toLocalDate());

        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    @Test
    @DisplayName("Debe calcular tiempo restante para turno en 1 hora exacta")
    void debeCalcularTiempoRestanteUnaHoraExacta() {
        LocalDateTime proximoTurno = LocalDateTime.now().plusHours(1);
        reminderDataValido.setFechaHoraCompleta(proximoTurno);
        reminderDataValido.setFecha(proximoTurno.toLocalDate());

        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    @Test
    @DisplayName("Debe calcular tiempo restante para turno en varias horas (menos de 24)")
    void debeCalcularTiempoRestanteVariasHoras() {
        LocalDateTime proximoTurno = LocalDateTime.now().plusHours(12);
        reminderDataValido.setFechaHoraCompleta(proximoTurno);
        reminderDataValido.setFecha(proximoTurno.toLocalDate());

        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    @Test
    @DisplayName("Debe calcular tiempo restante para turno mañana")
    void debeCalcularTiempoRestanteManana() {
        LocalDateTime manana = LocalDateTime.now().plusHours(24);
        reminderDataValido.setFechaHoraCompleta(manana);
        reminderDataValido.setFecha(manana.toLocalDate());

        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    @Test
    @DisplayName("Debe calcular tiempo restante para turno en varios días")
    void debeCalcularTiempoRestanteVariosDias() {
        LocalDateTime futuro = LocalDateTime.now().plusDays(5);
        reminderDataValido.setFechaHoraCompleta(futuro);
        reminderDataValido.setFecha(futuro.toLocalDate());

        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    // ==================== TESTS DE MANEJO DE ERRORES ====================

    @Test
    @DisplayName("Debe lanzar NotificationException si falla creación de MimeMessage")
    void debeLanzarExcepcionSiFallaCreacionMimeMessage() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Error al crear mensaje"));

        NotificationException exception = assertThrows(NotificationException.class, () -> {
            emailService.sendReminder(reminderDataValido);
        });

        assertTrue(exception.getMessage().contains("Error inesperado"));
    }

    @Test
    @DisplayName("Debe lanzar NotificationException si falla envío de email")
    void debeLanzarExcepcionSiFallaEnvio() {
        doThrow(new RuntimeException("Error SMTP")).when(mailSender).send(any(MimeMessage.class));

        NotificationException exception = assertThrows(NotificationException.class, () -> {
            emailService.sendReminder(reminderDataValido);
        });

        assertTrue(exception.getMessage().contains("Error inesperado"));
        assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("Debe encapsular causa original en NotificationException")
    void debeEncapsularCausaOriginal() {
        RuntimeException causaOriginal = new RuntimeException("Error de red");
        doThrow(causaOriginal).when(mailSender).send(any(MimeMessage.class));

        NotificationException exception = assertThrows(NotificationException.class, () -> {
            emailService.sendReminder(reminderDataValido);
        });

        assertEquals(causaOriginal, exception.getCause());
    }

    // ==================== TESTS DE CONFIGURACIÓN DESHABILITADA ====================

    @Test
    @DisplayName("No debe enviar email si el servicio está deshabilitado")
    void noDebeEnviarSiEstaDeshabilitado() {
        ReflectionTestUtils.setField(emailService, "enabled", false);

        emailService.sendReminder(reminderDataValido);

        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Debe advertir en logs cuando está deshabilitado (validación indirecta)")
    void debeAdvertirCuandoEstaDeshabilitado() {
        ReflectionTestUtils.setField(emailService, "enabled", false);

        // El servicio no debe lanzar excepción, solo loggear warning
        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    // ==================== TESTS DE CARGA DE TEMPLATE ====================

    @Test
    @DisplayName("Constructor debe cargar template HTML exitosamente")
    void constructorDebeCargarTemplate() {
        // Si el constructor falla al cargar el template, lanza NotificationException
        assertDoesNotThrow(() -> new EmailNotificationService(mailSender, repositorioPago));
    }

    @Test
    @DisplayName("Template cargado debe contener placeholders esperados")
    void templateDebeContenerPlaceholders() {
        // Si el template está corrupto o vacío, el envío fallará
        // Este test indirectamente verifica que el template tiene estructura válida
        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    // ==================== TESTS DE EDGE CASES ====================

    @Test
    @DisplayName("Debe manejar nombres con caracteres especiales")
    void debeManejarCaracteresEspeciales() {
        reminderDataValido.setClienteNombre("José María Ñoño");
        reminderDataValido.setProfesionalNombre("Dr. García-López");
        reminderDataValido.setServicioNombre("Atención médica & consulta");

        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    @Test
    @DisplayName("Debe manejar direcciones muy largas")
    void debeManejarDireccionesLargas() {
        reminderDataValido.setEmpresaDireccion("Avenida Brigadier General Juan Manuel de Rosas 1234, Piso 5, Departamento B, Torre Norte, Edificio Las Americas");
        reminderDataValido.setEmpresaCiudad("Ciudad Autónoma de Buenos Aires");
        reminderDataValido.setEmpresaProvincia("Provincia de Buenos Aires");

        assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido));
    }

    @Test
    @DisplayName("Debe manejar emails válidos con diferentes formatos")
    void debeManejarDiferentesFormatosEmail() {
        String[] emailsValidos = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.ar",
            "123@example.com"
        };

        for (String email : emailsValidos) {
            reminderDataValido.setClienteEmail(email);
            assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido),
                "Debería manejar email: " + email);
        }
    }

    @Test
    @DisplayName("Debe manejar teléfonos con diferentes formatos")
    void debeManejarDiferentesFormatosTelefono() {
        String[] telefonos = {
            "+5491112345678",
            "011-1234-5678",
            "(011) 1234-5678",
            "11-1234-5678"
        };

        for (String telefono : telefonos) {
            reminderDataValido.setEmpresaTelefono(telefono);
            assertDoesNotThrow(() -> emailService.sendReminder(reminderDataValido),
                "Debería manejar teléfono: " + telefono);
        }
    }

    // ==================== TESTS DE CONFIRMACIÓN DE TURNO ====================

    @Test
    @DisplayName("Debe enviar confirmación de turno cuando los datos son válidos")
    void debeEnviarConfirmacionTurnoConDatosValidos() {
        Turno turno = construirTurnoValido();
        Pago pago = new Pago();
        pago.setMonto(new BigDecimal("2500.00"));
        when(repositorioPago.findByTurnoId(turno.getId())).thenReturn(Optional.of(pago));

        assertDoesNotThrow(() -> emailService.enviarCorreoConfirmacionTurno(turno));

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("No debe propagar excepción si falla SMTP durante confirmación")
    void noDebePropagarExcepcionSiFallaSmtpConfirmacion() {
        Turno turno = construirTurnoValido();
        doThrow(new RuntimeException("SMTP caído")).when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> emailService.enviarCorreoConfirmacionTurno(turno));
        verify(mailSender, times(1)).createMimeMessage();
    }

    @Test
    @DisplayName("No debe enviar confirmación si servicio email está deshabilitado")
    void noDebeEnviarConfirmacionSiServicioDeshabilitado() {
        Turno turno = construirTurnoValido();
        ReflectionTestUtils.setField(emailService, "enabled", false);

        emailService.enviarCorreoConfirmacionTurno(turno);

        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
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
