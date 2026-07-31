package com.example.sitema_de_turnos.servicio.scheduler;

import com.example.sitema_de_turnos.modelo.BotConfiguracion;
import com.example.sitema_de_turnos.modelo.Cliente;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.EstadoTurno;
import com.example.sitema_de_turnos.repositorio.RepositorioBotConfiguracion;
import com.example.sitema_de_turnos.repositorio.RepositorioCliente;
import com.example.sitema_de_turnos.repositorio.RepositorioEmpresa;
import com.example.sitema_de_turnos.servicio.notificacion.WhatsAppNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WinBackSchedulerService Tests")
class WinBackSchedulerServiceTest {

    @Mock
    private RepositorioEmpresa repositorioEmpresa;

    @Mock
    private RepositorioCliente repositorioCliente;

    @Mock
    private RepositorioBotConfiguracion repositorioBotConfiguracion;

    @Mock
    private WhatsAppNotificationService whatsAppService;

    @InjectMocks
    private WinBackSchedulerService winBackSchedulerService;

    private Empresa empresa;
    private BotConfiguracion botConfig;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNombre("Test Barberia");
        empresa.setSlug("test-barberia");
        empresa.setActiva(true);
        empresa.setTimezone("America/Argentina/Buenos_Aires");
        empresa.setWinBackHabilitado(true);
        empresa.setWinBackDiasInactividad(45);
        empresa.setWinBackDiasEsperaReenvio(90);
        empresa.setWinBackMensajePlantilla("Hola {cliente}, vuelve! Descuento: {descuento}%. Link: {link}");
        empresa.setWinBackDescuentoPorcentaje(20);

        botConfig = new BotConfiguracion();
        botConfig.setId(1L);
        botConfig.setTenantId(1L);
        botConfig.setInstanciaWhatsapp("barberia_instance");
        botConfig.setEstadoBot(true);

        cliente = new Cliente();
        cliente.setId(10L);
        cliente.setNombre("Carlos Perez");
        cliente.setTelefono("5491112345678");
        cliente.setActivo(true);
        cliente.setEmpresa(empresa);
    }

    @Test
    @DisplayName("Debe procesar e invocar el webhook para clientes elegibles")
    void debeProcesarEInvocarWebhookParaClientesElegibles() {
        when(repositorioEmpresa.findByActivaTrue()).thenReturn(List.of(empresa));
        when(repositorioBotConfiguracion.findByTenantId(1L)).thenReturn(Optional.of(botConfig));
        
        when(repositorioCliente.findClientesParaRecuperacion(
                eq(1L), any(LocalDate.class), any(LocalDate.class), any(LocalDate.class), any(LocalTime.class), anyList()
        )).thenReturn(List.of(cliente));

        winBackSchedulerService.procesarRecuperacionClientes();

        verify(repositorioCliente, times(1)).save(cliente);
        verify(whatsAppService, times(1)).enviarMensajeConInstancia(
                eq("5491112345678"),
                contains("Carlos Perez"),
                eq("barberia_instance")
        );
        assertEquals(LocalDate.now(), cliente.getFechaUltimoMensajeRecuperacion());
        assertEquals(20, cliente.getWinBackDescuentoPendiente());
    }

    @Test
    @DisplayName("Debe saltar si WinBack esta deshabilitado en Empresa")
    void debeSaltarSiWinBackDeshabilitadoEnEmpresa() {
        empresa.setWinBackHabilitado(false);
        when(repositorioEmpresa.findByActivaTrue()).thenReturn(List.of(empresa));

        winBackSchedulerService.procesarRecuperacionClientes();

        verify(repositorioBotConfiguracion, never()).findByTenantId(anyLong());
        verify(repositorioCliente, never()).findClientesParaRecuperacion(anyLong(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Debe saltar si no hay configuracion de bot registrada")
    void debeSaltarSiNoHayConfiguracionDeBot() {
        when(repositorioEmpresa.findByActivaTrue()).thenReturn(List.of(empresa));
        when(repositorioBotConfiguracion.findByTenantId(1L)).thenReturn(Optional.empty());

        winBackSchedulerService.procesarRecuperacionClientes();

        verify(repositorioCliente, never()).findClientesParaRecuperacion(anyLong(), any(), any(), any(), any(), any());
    }
}
