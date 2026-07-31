package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.publico.CrearTurnoRequest;
import com.example.sitema_de_turnos.dto.publico.TurnoResponsePublico;
import com.example.sitema_de_turnos.modelo.*;
import com.example.sitema_de_turnos.repositorio.*;
import com.example.sitema_de_turnos.servicio.notificacion.EmailNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServicioTurno Win-Back Discount Tests")
class ServicioTurnoWinBackTest {

    @Mock private RepositorioTurno repositorioTurno;
    @Mock private RepositorioCliente repositorioCliente;
    @Mock private RepositorioServicio repositorioServicio;
    @Mock private RepositorioPerfilProfesional repositorioPerfilProfesional;
    @Mock private RepositorioPerfilDueno repositorioPerfilDueno;
    @Mock private RepositorioEmpresa repositorioEmpresa;
    @Mock private RepositorioBloqueoFecha repositorioBloqueoFecha;
    @Mock private RepositorioDisponibilidadProfesional repositorioDisponibilidadProfesional;
    @Mock private RepositorioPago repositorioPago;
    @Mock private ServicioNotificacion servicioNotificacion;
    @Mock private ServicioPoliticaCancelacion servicioPoliticaCancelacion;
    @Mock private ServicioPublico servicioPublico;
    @Mock private EmailNotificationService emailNotificationService;

    private ServicioTurno servicioTurno;

    private Empresa empresa;
    private Servicio servicio;
    private PerfilProfesional profesional;
    private Cliente cliente;
    private CrearTurnoRequest request;

    @BeforeEach
    void setUp() {
        servicioTurno = new ServicioTurno(
            repositorioTurno,
            repositorioCliente,
            repositorioServicio,
            repositorioPerfilProfesional,
            repositorioPerfilDueno,
            repositorioEmpresa,
            repositorioBloqueoFecha,
            repositorioDisponibilidadProfesional,
            repositorioPago,
            servicioNotificacion,
            servicioPoliticaCancelacion,
            servicioPublico,
            emailNotificationService
        );

        empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNombre("Test Barber");
        empresa.setSlug("test-barber");
        empresa.setTimezone("America/Argentina/Buenos_Aires");
        empresa.setBufferPorDefecto(10);

        servicio = new Servicio();
        servicio.setId(2L);
        servicio.setNombre("Corte");
        servicio.setDuracionMinutos(30);
        servicio.setPrecio(BigDecimal.valueOf(1000));
        servicio.setRequiereSena(false);
        servicio.setEmpresa(empresa);

        Usuario usuarioProfesional = new Usuario();
        usuarioProfesional.setId(5L);
        usuarioProfesional.setNombre("Pedro");
        usuarioProfesional.setApellido("Gomez");
        usuarioProfesional.setEmail("pedro@example.com");

        profesional = new PerfilProfesional();
        profesional.setId(3L);
        profesional.setActivo(true);
        profesional.setEmpresa(empresa);
        profesional.setUsuario(usuarioProfesional);

        cliente = new Cliente();
        cliente.setId(4L);
        cliente.setNombre("Juan");
        cliente.setEmail("juan@example.com");
        cliente.setEmpresa(empresa);
        cliente.setWinBackDescuentoPendiente(15); // 15% discount pending

        request = new CrearTurnoRequest();
        request.setServicioId(2L);
        request.setProfesionalId(3L);
        request.setFecha(LocalDate.now().plusDays(2).toString());
        request.setHoraInicio("10:00");
        request.setNombreCliente("Juan");
        request.setEmailCliente("juan@example.com");
    }

    @Test
    @DisplayName("Debe aplicar descuento de Win-Back, guardar historiales en el turno y limpiar la bandera del cliente")
    void debeAplicarDescuentoWinBackYLimpiarBanderaCliente() {
        when(repositorioEmpresa.findBySlugAndActivaTrue("test-barber")).thenReturn(Optional.of(empresa));
        when(repositorioServicio.findById(2L)).thenReturn(Optional.of(servicio));
        when(repositorioPerfilProfesional.findById(3L)).thenReturn(Optional.of(profesional));
        when(repositorioTurno.existeSolapamiento(eq(profesional), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class), anyList()))
                .thenReturn(false);
        when(repositorioBloqueoFecha.findBloqueoEnFecha(eq(profesional), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        
        when(servicioPoliticaCancelacion.obtenerActivaPorEmpresaYTipo(eq(empresa), eq(TipoPoliticaCancelacion.CANCELACION)))
                .thenReturn(Optional.empty());
        when(servicioPoliticaCancelacion.obtenerActivaPorEmpresaYTipo(eq(empresa), eq(TipoPoliticaCancelacion.AMBOS)))
                .thenReturn(Optional.empty());

        when(repositorioTurno.findByClienteEmailIgnoreCaseAndFechaAndEstadoIn(anyString(), any(LocalDate.class), anyList()))
                .thenReturn(Collections.emptyList());

        // Mock save returning the same objects
        ArgumentCaptor<Turno> turnoCaptor = ArgumentCaptor.forClass(Turno.class);
        when(repositorioTurno.save(turnoCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repositorioCliente.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TurnoResponsePublico response = servicioTurno.crearTurnoPublico("test-barber", cliente, request);

        assertNotNull(response);
        Turno turnoGuardado = turnoCaptor.getValue();

        // Assert final price: 1000 - 15% = 850
        assertEquals(BigDecimal.valueOf(850.00).setScale(2), turnoGuardado.getPrecio());
        assertEquals(BigDecimal.valueOf(1000), turnoGuardado.getPrecioOriginal());
        assertEquals(15, turnoGuardado.getWinBackDescuentoAplicado());

        // Verify discount was consumed on cliente
        assertNull(cliente.getWinBackDescuentoPendiente());
        verify(repositorioCliente, times(1)).save(cliente);
    }
}
