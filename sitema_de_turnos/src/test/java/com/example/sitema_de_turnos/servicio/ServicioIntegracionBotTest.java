package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.bot.BotCancelarTurnoRequestDto;
import com.example.sitema_de_turnos.dto.bot.BotReprogramarTurnoRequestDto;
import com.example.sitema_de_turnos.dto.bot.BotReprogramarTurnoResponseDto;
import com.example.sitema_de_turnos.dto.bot.BotTurnoConfirmadoResponseDto;
import com.example.sitema_de_turnos.dto.publico.ReservaReprogramarRequest;
import com.example.sitema_de_turnos.dto.publico.TurnoResponsePublico;
import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.excepcion.ValidacionException;
import com.example.sitema_de_turnos.modelo.*;
import com.example.sitema_de_turnos.repositorio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioIntegracionBotTest {

    @Mock private RepositorioBotConfiguracion repositorioBotConfiguracion;
    @Mock private RepositorioCliente repositorioCliente;
    @Mock private RepositorioEmpresa repositorioEmpresa;
    @Mock private RepositorioServicio repositorioServicio;
    @Mock private RepositorioPerfilProfesional repositorioPerfilProfesional;
    @Mock private RepositorioProfesionalServicio repositorioProfesionalServicio;
    @Mock private RepositorioDisponibilidadProfesional repositorioDisponibilidadProfesional;
    @Mock private RepositorioTurno repositorioTurno;
    @Mock private ServicioTurno servicioTurno;
    @Mock private ServicioPublico servicioPublico;

    private ServicioIntegracionBot servicioIntegracionBot;

    private Empresa empresa;
    private Cliente cliente;
    private PerfilProfesional profesional;
    private Usuario usuarioProfesional;
    private Servicio servicio;
    private Turno turno;

    @BeforeEach
    void setUp() {
        servicioIntegracionBot = new ServicioIntegracionBot(
            repositorioBotConfiguracion,
            repositorioCliente,
            repositorioEmpresa,
            repositorioServicio,
            repositorioPerfilProfesional,
            repositorioProfesionalServicio,
            repositorioDisponibilidadProfesional,
            repositorioTurno,
            servicioTurno,
            servicioPublico
        );

        empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNombre("Estetica Express");
        empresa.setSlug("estetica-express");
        empresa.setActiva(true);
        empresa.setTimezone("America/Argentina/Buenos_Aires");

        cliente = new Cliente();
        cliente.setId(10L);
        cliente.setNombre("Juan Perez");
        cliente.setTelefono("5491112345678");
        cliente.setEmpresa(empresa);

        usuarioProfesional = new Usuario();
        usuarioProfesional.setId(20L);
        usuarioProfesional.setNombre("Carlos");
        usuarioProfesional.setApellido("Gomez");
        usuarioProfesional.setEmail("carlos@estetica.com");

        profesional = new PerfilProfesional();
        profesional.setId(30L);
        profesional.setUsuario(usuarioProfesional);
        profesional.setEmpresa(empresa);

        servicio = new Servicio();
        servicio.setId(40L);
        servicio.setNombre("Masaje");
        servicio.setPrecio(BigDecimal.valueOf(1500.0));
        servicio.setDuracionMinutos(30);

        turno = new Turno();
        turno.setId(100L);
        turno.setEmpresa(empresa);
        turno.setCliente(cliente);
        turno.setProfesional(profesional);
        turno.setServicio(servicio);
        turno.setFecha(LocalDate.now().plusDays(2));
        turno.setHoraInicio(LocalTime.of(14, 0));
        turno.setHoraFin(LocalTime.of(14, 30));
        turno.setPrecio(BigDecimal.valueOf(1500.0));
        turno.setEstado(EstadoTurno.CONFIRMADO);
        turno.setObservaciones("Turno original");
    }

    @Test
    @DisplayName("buscarTurnosVigentesConfirmados: Tenant inactivo lanza excepción")
    void buscarTurnosVigentesConfirmados_tenantInactivo_lanzaExcepcion() {
        empresa.setActiva(false);
        when(repositorioEmpresa.findById(1L)).thenReturn(Optional.of(empresa));

        assertThrows(ValidacionException.class, () ->
            servicioIntegracionBot.buscarTurnosVigentesConfirmados(1L, "123456")
        );
    }

    @Test
    @DisplayName("buscarTurnosVigentesConfirmados: Cliente no registrado retorna lista vacía")
    void buscarTurnosVigentesConfirmados_clienteNoRegistrado_retornaVacio() {
        when(repositorioEmpresa.findById(1L)).thenReturn(Optional.of(empresa));
        when(repositorioCliente.findByEmpresaAndTelefonoAndActivoTrue(eq(empresa), anyString()))
            .thenReturn(Optional.empty());

        List<BotTurnoConfirmadoResponseDto> result = servicioIntegracionBot.buscarTurnosVigentesConfirmados(1L, "5491112345678");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("buscarTurnosVigentesConfirmados: Retorna turnos confirmados vigentes")
    void buscarTurnosVigentesConfirmados_happyPath_retornaLista() {
        when(repositorioEmpresa.findById(1L)).thenReturn(Optional.of(empresa));
        when(repositorioCliente.findByEmpresaAndTelefonoAndActivoTrue(empresa, "5491112345678"))
            .thenReturn(Optional.of(cliente));

        when(repositorioTurno.findTurnosVigentesPorClienteAndEstado(
            eq(cliente), eq(EstadoTurno.CONFIRMADO), any(LocalDate.class), any(LocalTime.class)
        )).thenReturn(List.of(turno));

        List<BotTurnoConfirmadoResponseDto> result = servicioIntegracionBot.buscarTurnosVigentesConfirmados(1L, "+54 9 11 1234-5678");

        assertEquals(1, result.size());
        BotTurnoConfirmadoResponseDto responseDto = result.get(0);
        assertEquals(turno.getId(), responseDto.getTurnoId());
        assertEquals("Masaje", responseDto.getServicioNombre());
        assertEquals("Carlos Gomez", responseDto.getProfesionalNombre());
        assertEquals(turno.getFecha().toString(), responseDto.getFecha());
    }

    @Test
    @DisplayName("cancelarTurnoPorBot: Happy Path ejecuta correctamente")
    void cancelarTurnoPorBot_happyPath() {
        when(repositorioEmpresa.findById(1L)).thenReturn(Optional.of(empresa));
        when(repositorioTurno.findById(100L)).thenReturn(Optional.of(turno));
        when(repositorioCliente.findByEmpresaAndTelefonoAndActivoTrue(empresa, "5491112345678"))
            .thenReturn(Optional.of(cliente));

        BotCancelarTurnoRequestDto request = new BotCancelarTurnoRequestDto("+54 9 11 1234-5678", "Me enfermé");

        assertDoesNotThrow(() ->
            servicioIntegracionBot.cancelarTurnoPorBot(1L, 100L, request)
        );

        verify(servicioTurno).cancelarTurnoPorCliente(100L, cliente, "Me enfermé");
    }

    @Test
    @DisplayName("cancelarTurnoPorBot: Intento de cancelar turno de otro cliente lanza AccesoDenegadoException")
    void cancelarTurnoPorBot_ajeno_lanzaAccesoDenegado() {
        Cliente otroCliente = new Cliente();
        otroCliente.setId(999L);
        otroCliente.setNombre("Otro");
        otroCliente.setTelefono("5491199999999");
        otroCliente.setEmpresa(empresa);

        when(repositorioEmpresa.findById(1L)).thenReturn(Optional.of(empresa));
        when(repositorioTurno.findById(100L)).thenReturn(Optional.of(turno)); // Turno pertenece a cliente(10L)
        when(repositorioCliente.findByEmpresaAndTelefonoAndActivoTrue(empresa, "5491199999999"))
            .thenReturn(Optional.of(otroCliente));

        BotCancelarTurnoRequestDto request = new BotCancelarTurnoRequestDto("+54 9 11 9999-9999", "Me enfermé");

        assertThrows(AccesoDenegadoException.class, () ->
            servicioIntegracionBot.cancelarTurnoPorBot(1L, 100L, request)
        );
        verify(servicioTurno, never()).cancelarTurnoPorCliente(anyLong(), any(Cliente.class), anyString());
    }

    @Test
    @DisplayName("reprogramarTurnoPorBot: Happy Path ejecuta y devuelve nuevo turno")
    void reprogramarTurnoPorBot_happyPath() {
        when(repositorioEmpresa.findById(1L)).thenReturn(Optional.of(empresa));
        when(repositorioTurno.findById(100L)).thenReturn(Optional.of(turno));
        when(repositorioCliente.findByEmpresaAndTelefonoAndActivoTrue(empresa, "5491112345678"))
            .thenReturn(Optional.of(cliente));

        LocalDateTime nuevaFechaHora = LocalDateTime.of(2026, 7, 20, 10, 0);
        BotReprogramarTurnoRequestDto request = new BotReprogramarTurnoRequestDto(
            "+54 9 11 1234-5678", nuevaFechaHora, 30L
        );

        TurnoResponsePublico responseMock = new TurnoResponsePublico();
        responseMock.setId(200L);
        responseMock.setServicioId(40L);
        responseMock.setProfesionalId(30L);
        responseMock.setEstado("CONFIRMADO");
        responseMock.setFecha("2026-07-20");
        responseMock.setHoraInicio("10:00");
        responseMock.setHoraFin("10:30");

        when(servicioTurno.reprogramarReservaPorCliente(eq(100L), eq(cliente), any(ReservaReprogramarRequest.class)))
            .thenReturn(responseMock);

        BotReprogramarTurnoResponseDto response = servicioIntegracionBot.reprogramarTurnoPorBot(1L, 100L, request);

        assertNotNull(response);
        assertEquals(200L, response.getTurnoId());
        assertEquals("2026-07-20", response.getFecha());
        assertEquals("10:00", response.getHoraInicio());

        ArgumentCaptor<ReservaReprogramarRequest> captor = ArgumentCaptor.forClass(ReservaReprogramarRequest.class);
        verify(servicioTurno).reprogramarReservaPorCliente(eq(100L), eq(cliente), captor.capture());
        ReservaReprogramarRequest sentRequest = captor.getValue();
        assertEquals("2026-07-20", sentRequest.getFecha());
        assertEquals("10:00", sentRequest.getHoraInicio());
        assertEquals(30L, sentRequest.getProfesionalId());
    }
}
