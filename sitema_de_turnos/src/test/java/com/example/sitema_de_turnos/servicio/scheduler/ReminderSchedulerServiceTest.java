package com.example.sitema_de_turnos.servicio.scheduler;

import com.example.sitema_de_turnos.dto.notificacion.ReminderData;
import com.example.sitema_de_turnos.excepcion.NotificationException;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.EstadoTurno;
import com.example.sitema_de_turnos.modelo.Turno;
import com.example.sitema_de_turnos.repositorio.RepositorioEmpresa;
import com.example.sitema_de_turnos.repositorio.RepositorioTurno;
import com.example.sitema_de_turnos.servicio.notificacion.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests exhaustivos para ReminderSchedulerService
 * Cubre: programación, envío, reintentos, actualizaciones de estado, edge cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReminderSchedulerService Tests")
class ReminderSchedulerServiceTest {

    @Mock
    private RepositorioTurno repositorioTurno;

    @Mock
    private RepositorioEmpresa repositorioEmpresa;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ReminderConfig config;

    @InjectMocks
    private ReminderSchedulerService schedulerService;

    private Empresa empresaActiva;
    private Turno turnoConfirmado;

    @BeforeEach
    void setUp() {
        // Configuración por defecto (usando lenient para evitar UnnecessaryStubbing en tests que no usan todos los stubs)
        lenient().when(config.isEnabled()).thenReturn(true);
        lenient().when(config.getHoursBefore()).thenReturn(24);
        lenient().when(config.getMinMinutesBefore()).thenReturn(30);
        lenient().when(config.getMaxRetries()).thenReturn(3);
        lenient().when(config.getRetryDelayMs()).thenReturn(100L); // Reducido para tests rápidos

        // Empresa activa con recordatorios habilitados
        empresaActiva = new Empresa();
        empresaActiva.setId(1L);
        empresaActiva.setNombre("Test Empresa");
        empresaActiva.setActiva(true);
        empresaActiva.setEnviarRecordatorios(true);
        empresaActiva.setTimezone("America/Argentina/Buenos_Aires");

        // Turno confirmado pendiente de recordatorio
        turnoConfirmado = TestDataBuilder.crearTurnoConfirmado();
        turnoConfirmado.setRecordatorioEnviado(false);
        turnoConfirmado.setRecordatorioIntentos(0);
    }

    // ==================== TESTS DE CONFIGURACIÓN ====================

    @Test
    @DisplayName("Debe saltear procesamiento si scheduler está deshabilitado")
    void debeSaltearSiSchedulerDeshabilitado() {
        when(config.isEnabled()).thenReturn(false);

        schedulerService.procesarRecordatorios();

        verify(repositorioEmpresa, never()).findByActivaTrue();
        verify(notificationService, never()).sendReminder(any());
    }

    @Test
    @DisplayName("Debe abortar si servicio de notificación no está disponible")
    void debeAbortarSiServicioNoDisponible() {
        when(notificationService.isAvailable()).thenReturn(false);

        schedulerService.procesarRecordatorios();

        verify(repositorioEmpresa, never()).findByActivaTrue();
        verify(notificationService, never()).sendReminder(any());
    }

    // ==================== TESTS DE PROCESAMIENTO DE EMPRESAS ====================

    @Test
    @DisplayName("Debe procesar todas las empresas activas")
    void debeProcesarTodasEmpresasActivas() {
        Empresa empresa1 = crearEmpresa(1L, "Empresa 1", true);
        Empresa empresa2 = crearEmpresa(2L, "Empresa 2", true);
        
        when(repositorioEmpresa.findByActivaTrue()).thenReturn(Arrays.asList(empresa1, empresa2));
        when(notificationService.isAvailable()).thenReturn(true);
        when(repositorioTurno.findTurnosPendientesRecordatorio(
            anyLong(), any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());

        schedulerService.procesarRecordatorios();

        verify(repositorioTurno, times(2)).findTurnosPendientesRecordatorio(
            anyLong(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Debe saltear empresas con recordatorios deshabilitados")
    void debeSaltearEmpresasConRecordatoriosDeshabilitados() {
        empresaActiva.setEnviarRecordatorios(false);
        when(repositorioEmpresa.findByActivaTrue()).thenReturn(Collections.singletonList(empresaActiva));
        when(notificationService.isAvailable()).thenReturn(true);

        schedulerService.procesarRecordatorios();

        verify(repositorioTurno, never()).findTurnosPendientesRecordatorio(
            anyLong(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Debe usar configuración de horas antes de la empresa si está definida")
    void debeUsarHorasAntesDeEmpresa() {
        empresaActiva.setHorasAntesRecordatorio(48);
        when(repositorioEmpresa.findByActivaTrue()).thenReturn(Collections.singletonList(empresaActiva));
        when(notificationService.isAvailable()).thenReturn(true);
        when(repositorioTurno.findTurnosPendientesRecordatorio(
            anyLong(), any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());

        schedulerService.procesarRecordatorios();

        ArgumentCaptor<LocalDate> fechaMaxCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(repositorioTurno).findTurnosPendientesRecordatorio(
            eq(1L), any(), any(), fechaMaxCaptor.capture(), any(), eq(EstadoTurno.CONFIRMADO));

        // Verificar que se usaron 48 horas en lugar de las 24 por defecto
        LocalDate fechaEsperada = LocalDate.now().plusDays(2);
        assertEquals(fechaEsperada, fechaMaxCaptor.getValue());
    }

    // ==================== TESTS DE ENVÍO EXITOSO ====================

    @Test
    @DisplayName("Debe enviar recordatorio exitosamente al primer intento")
    void debeEnviarRecordatorioExitoso() throws Exception {
        when(repositorioEmpresa.findByActivaTrue()).thenReturn(Collections.singletonList(empresaActiva));
        when(notificationService.isAvailable()).thenReturn(true);
        when(repositorioTurno.findTurnosPendientesRecordatorio(
            anyLong(), any(), any(), any(), any(), any())).thenReturn(Collections.singletonList(turnoConfirmado));
        
        // Simular envío exitoso
        doNothing().when(notificationService).sendReminder(any(ReminderData.class));

        schedulerService.procesarRecordatorios();

        // Verificar que se envió el recordatorio
        verify(notificationService, times(1)).sendReminder(any(ReminderData.class));
        
        // Verificar que se actualizó el turno correctamente
        // Se llama save() 2 veces: marcarInicioProcesamiento y actualizarTurnoExitoso
        verify(repositorioTurno, times(2)).save(turnoConfirmado);
        assertTrue(turnoConfirmado.getRecordatorioEnviado());
        assertNotNull(turnoConfirmado.getFechaRecordatorioEnviado());
        assertEquals(1, turnoConfirmado.getRecordatorioIntentos());
        assertNull(turnoConfirmado.getRecordatorioError());
    }

    @Test
    @DisplayName("Debe construir ReminderData con todos los campos correctos")
    void debeConstruirReminderDataCorrectamente() {
        when(repositorioEmpresa.findByActivaTrue()).thenReturn(Collections.singletonList(empresaActiva));
        when(notificationService.isAvailable()).thenReturn(true);
        when(repositorioTurno.findTurnosPendientesRecordatorio(
            anyLong(), any(), any(), any(), any(), any())).thenReturn(Collections.singletonList(turnoConfirmado));

        ArgumentCaptor<ReminderData> reminderCaptor = ArgumentCaptor.forClass(ReminderData.class);

        schedulerService.procesarRecordatorios();

        verify(notificationService).sendReminder(reminderCaptor.capture());
        
        ReminderData reminder = reminderCaptor.getValue();
        assertEquals(turnoConfirmado.getId(), reminder.getTurnoId());
        assertEquals(turnoConfirmado.getCliente().getEmail(), reminder.getClienteEmail());
        assertEquals(turnoConfirmado.getCliente().getNombre(), reminder.getClienteNombre());
        assertEquals(turnoConfirmado.getFecha(), reminder.getFecha());
        assertEquals(turnoConfirmado.getHoraInicio(), reminder.getHoraInicio());
        assertEquals(turnoConfirmado.getServicio().getNombre(), reminder.getServicioNombre());
        assertEquals(turnoConfirmado.getProfesional().getUsuario().getNombre(), reminder.getProfesionalNombre());
        assertEquals(empresaActiva.getNombre(), reminder.getEmpresaNombre());
    }

    // ==================== TESTS DE REINTENTOS ====================

    @Test
    @DisplayName("Debe reintentar hasta 3 veces ante fallo transitorio")
    void debeReintentarHastaTresVeces() throws Exception {
        when(repositorioEmpresa.findByActivaTrue()).thenReturn(Collections.singletonList(empresaActiva));
        when(notificationService.isAvailable()).thenReturn(true);
        when(repositorioTurno.findTurnosPendientesRecordatorio(
            anyLong(), any(), any(), any(), any(), any())).thenReturn(Collections.singletonList(turnoConfirmado));
        
        // Simular fallo en los primeros 2 intentos, éxito en el 3ro
        doThrow(new NotificationException("Error temporal"))
            .doThrow(new NotificationException("Error temporal"))
            .doNothing()
            .when(notificationService).sendReminder(any(ReminderData.class));

        schedulerService.procesarRecordatorios();

        // Verificar 3 intentos
        verify(notificationService, times(3)).sendReminder(any(ReminderData.class));
        
        // Verificar que finalmente se marcó como exitoso
        assertTrue(turnoConfirmado.getRecordatorioEnviado());
        assertEquals(3, turnoConfirmado.getRecordatorioIntentos());
        assertNull(turnoConfirmado.getRecordatorioError());
    }

    @Test
    @DisplayName("Debe fallar definitivamente después de 4 intentos fallidos")
    void debeFallarDespuesDeCuatroIntentos() {
        when(repositorioEmpresa.findByActivaTrue()).thenReturn(Collections.singletonList(empresaActiva));
        when(notificationService.isAvailable()).thenReturn(true);
        when(repositorioTurno.findTurnosPendientesRecordatorio(
            anyLong(), any(), any(), any(), any(), any())).thenReturn(Collections.singletonList(turnoConfirmado));
        
        // Simular fallo en todos los intentos
        doThrow(new NotificationException("Error persistente"))
            .when(notificationService).sendReminder(any(ReminderData.class));

        schedulerService.procesarRecordatorios();

        // Verificar 4 intentos (1 inicial + 3 reintentos)
        verify(notificationService, times(4)).sendReminder(any(ReminderData.class));
        
        // Verificar que se registró el error
        assertFalse(turnoConfirmado.getRecordatorioEnviado());
        assertEquals(4, turnoConfirmado.getRecordatorioIntentos());
        assertNotNull(turnoConfirmado.getRecordatorioError());
        assertTrue(turnoConfirmado.getRecordatorioError().contains("Error persistente"));
    }

    @Test
    @DisplayName("Debe truncar mensajes de error muy largos a 500 caracteres")
    void debeTruncarErroresLargos() {
        String errorMuyLargo = "X".repeat(600);
        
        when(repositorioEmpresa.findByActivaTrue()).thenReturn(Collections.singletonList(empresaActiva));
        when(notificationService.isAvailable()).thenReturn(true);
        when(repositorioTurno.findTurnosPendientesRecordatorio(
            anyLong(), any(), any(), any(), any(), any())).thenReturn(Collections.singletonList(turnoConfirmado));
        
        doThrow(new NotificationException(errorMuyLargo))
            .when(notificationService).sendReminder(any(ReminderData.class));

        schedulerService.procesarRecordatorios();

        // Verificar que el error se truncó
        assertEquals(500, turnoConfirmado.getRecordatorioError().length());
    }

    // ==================== TESTS DE MARCA DE PROCESAMIENTO ====================

    @Test
    @DisplayName("Debe marcar turno como 'en procesamiento' antes de enviar")
    void debeMarcarTurnoEnProcesamiento() {
        when(repositorioEmpresa.findByActivaTrue()).thenReturn(Collections.singletonList(empresaActiva));
        when(notificationService.isAvailable()).thenReturn(true);
        when(repositorioTurno.findTurnosPendientesRecordatorio(
            anyLong(), any(), any(), any(), any(), any())).thenReturn(Collections.singletonList(turnoConfirmado));

        schedulerService.procesarRecordatorios();

        // Verificar que se seteo recordatorioPrimerIntento
        assertNotNull(turnoConfirmado.getRecordatorioPrimerIntento());
        verify(repositorioTurno, atLeastOnce()).save(turnoConfirmado);
    }

    @Test
    @DisplayName("Debe resetear marca de procesamiento si todos los intentos fallan")
    void debeResetearMarcaSiTodosFallan() {
        when(repositorioEmpresa.findByActivaTrue()).thenReturn(Collections.singletonList(empresaActiva));
        when(notificationService.isAvailable()).thenReturn(true);
        when(repositorioTurno.findTurnosPendientesRecordatorio(
            anyLong(), any(), any(), any(), any(), any())).thenReturn(Collections.singletonList(turnoConfirmado));
        
        doThrow(new NotificationException("Error"))
            .when(notificationService).sendReminder(any(ReminderData.class));

        schedulerService.procesarRecordatorios();

        // Verificar que se reseteo la marca para permitir reintentos futuros
        assertNull(turnoConfirmado.getRecordatorioPrimerIntento());
    }

    // ==================== TESTS DE MÁXIMO DE REINTENTOS ====================

    @Test
    @DisplayName("Debe saltear turno que ya alcanzó el máximo de reintentos")
    void debeSaltearTurnoConMaximosReintentos() {
        turnoConfirmado.setRecordatorioIntentos(3);
        
        when(repositorioEmpresa.findByActivaTrue()).thenReturn(Collections.singletonList(empresaActiva));
        when(notificationService.isAvailable()).thenReturn(true);
        when(repositorioTurno.findTurnosPendientesRecordatorio(
            anyLong(), any(), any(), any(), any(), any())).thenReturn(Collections.singletonList(turnoConfirmado));

        schedulerService.procesarRecordatorios();

        // Verificar que NO se intentó enviar
        verify(notificationService, never()).sendReminder(any());
        
        // Verificar que se marcó como enviado con error
        assertTrue(turnoConfirmado.getRecordatorioEnviado());
        assertEquals("Máximo de reintentos alcanzado", turnoConfirmado.getRecordatorioError());
    }

    // ==================== TESTS DE VALIDACIÓN DE VENTANA TEMPORAL ====================

    @Test
    @DisplayName("Debe respetar ventana temporal (minMinutesBefore a hoursBefore)")
    void debeRespetarVentanaTemporal() {
        when(repositorioEmpresa.findByActivaTrue()).thenReturn(Collections.singletonList(empresaActiva));
        when(notificationService.isAvailable()).thenReturn(true);
        when(repositorioTurno.findTurnosPendientesRecordatorio(
            anyLong(), any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());

        schedulerService.procesarRecordatorios();

        ArgumentCaptor<LocalDate> fechaMinCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalTime> horaMinCaptor = ArgumentCaptor.forClass(LocalTime.class);
        ArgumentCaptor<LocalDate> fechaMaxCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalTime> horaMaxCaptor = ArgumentCaptor.forClass(LocalTime.class);

        verify(repositorioTurno).findTurnosPendientesRecordatorio(
            eq(1L), 
            fechaMinCaptor.capture(), 
            horaMinCaptor.capture(), 
            fechaMaxCaptor.capture(), 
            horaMaxCaptor.capture(), 
            eq(EstadoTurno.CONFIRMADO)
        );

        // Verificar que los límites son razonables
        assertNotNull(fechaMinCaptor.getValue());
        assertNotNull(fechaMaxCaptor.getValue());
        assertTrue(fechaMinCaptor.getValue().isBefore(fechaMaxCaptor.getValue()) || 
                   fechaMinCaptor.getValue().equals(fechaMaxCaptor.getValue()));
    }

    // ==================== TESTS DE MANEJO DE ERRORES ====================

    @Test
    @DisplayName("Debe continuar procesando otras empresas si una falla")
    void debeContinuarConOtrasEmpresasSiUnaFalla() {
        Empresa empresa1 = crearEmpresa(1L, "Empresa 1", true);
        Empresa empresa2 = crearEmpresa(2L, "Empresa 2", true);
        
        when(repositorioEmpresa.findByActivaTrue()).thenReturn(Arrays.asList(empresa1, empresa2));
        when(notificationService.isAvailable()).thenReturn(true);
        
        // Primera empresa lanza excepción, segunda no
        when(repositorioTurno.findTurnosPendientesRecordatorio(
            eq(1L), any(), any(), any(), any(), any())).thenThrow(new RuntimeException("Error BD"));
        when(repositorioTurno.findTurnosPendientesRecordatorio(
            eq(2L), any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());

        schedulerService.procesarRecordatorios();

        // Verificar que se procesaron ambas empresas
        verify(repositorioTurno).findTurnosPendientesRecordatorio(
            eq(1L), any(), any(), any(), any(), any());
        verify(repositorioTurno).findTurnosPendientesRecordatorio(
            eq(2L), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Debe manejar InterruptedException correctamente")
    void debeManejarInterruptedException() {
        when(repositorioEmpresa.findByActivaTrue()).thenReturn(Collections.singletonList(empresaActiva));
        when(notificationService.isAvailable()).thenReturn(true);
        when(repositorioTurno.findTurnosPendientesRecordatorio(
            anyLong(), any(), any(), any(), any(), any())).thenReturn(Collections.singletonList(turnoConfirmado));
        
        // Simular fallo que cause InterruptedException en el retry
        doThrow(new NotificationException("Error"))
            .when(notificationService).sendReminder(any(ReminderData.class));
        
        // Interrumpir el hilo actual para simular interrupción
        Thread.currentThread().interrupt();

        schedulerService.procesarRecordatorios();

        // Verificar que se reseteo la marca
        assertNull(turnoConfirmado.getRecordatorioPrimerIntento());
    }

    // ==================== HELPERS ====================

    private Empresa crearEmpresa(Long id, String nombre, boolean enviarRecordatorios) {
        Empresa empresa = new Empresa();
        empresa.setId(id);
        empresa.setNombre(nombre);
        empresa.setActiva(true);
        empresa.setEnviarRecordatorios(enviarRecordatorios);
        empresa.setTimezone("America/Argentina/Buenos_Aires");
        return empresa;
    }
}
