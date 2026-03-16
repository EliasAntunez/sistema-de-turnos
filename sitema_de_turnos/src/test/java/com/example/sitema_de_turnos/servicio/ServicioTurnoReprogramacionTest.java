package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.excepcion.ValidacionException;
import com.example.sitema_de_turnos.modelo.Cliente;
import com.example.sitema_de_turnos.modelo.DisponibilidadProfesional;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.EstadoTurno;
import com.example.sitema_de_turnos.modelo.PerfilProfesional;
import com.example.sitema_de_turnos.modelo.Servicio;
import com.example.sitema_de_turnos.modelo.Turno;
import com.example.sitema_de_turnos.repositorio.RepositorioBloqueoFecha;
import com.example.sitema_de_turnos.repositorio.RepositorioCliente;
import com.example.sitema_de_turnos.repositorio.RepositorioDisponibilidadProfesional;
import com.example.sitema_de_turnos.repositorio.RepositorioEmpresa;
import com.example.sitema_de_turnos.repositorio.RepositorioPago;
import com.example.sitema_de_turnos.repositorio.RepositorioPerfilDueno;
import com.example.sitema_de_turnos.repositorio.RepositorioPerfilProfesional;
import com.example.sitema_de_turnos.repositorio.RepositorioServicio;
import com.example.sitema_de_turnos.repositorio.RepositorioTurno;
import com.example.sitema_de_turnos.servicio.notificacion.EmailNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioTurnoReprogramacionTest {

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
    }

    @Test
    @DisplayName("testReprogramacionMantieneEstadoPendientePago")
    void testReprogramacionMantieneEstadoPendientePago() {
        Turno turnoOriginal = construirTurnoBase(EstadoTurno.PENDIENTE_PAGO, 0);
        PerfilProfesional profesionalDestino = turnoOriginal.getProfesional();
        LocalDate nuevaFecha = LocalDate.now().plusDays(2);
        LocalTime nuevaHoraInicio = LocalTime.of(10, 0);

        prepararStubsHappyPath(turnoOriginal, profesionalDestino);

        Turno turnoReprogramado = invocarAplicarReprogramacionSegura(
            turnoOriginal,
            profesionalDestino,
            nuevaFecha,
            nuevaHoraInicio,
            "CLIENTE"
        );

        assertEquals(EstadoTurno.PENDIENTE_PAGO, turnoReprogramado.getEstado());
    }

    @Test
    @DisplayName("testLimiteReprogramacionClienteLanzaExcepcion")
    void testLimiteReprogramacionClienteLanzaExcepcion() {
        Turno turnoOriginal = construirTurnoBase(EstadoTurno.CONFIRMADO, 2);
        PerfilProfesional profesionalDestino = turnoOriginal.getProfesional();
        LocalDate nuevaFecha = LocalDate.now().plusDays(2);
        LocalTime nuevaHoraInicio = LocalTime.of(11, 0);

        ValidacionException ex = assertThrows(ValidacionException.class, () ->
            invocarAplicarReprogramacionSegura(
                turnoOriginal,
                profesionalDestino,
                nuevaFecha,
                nuevaHoraInicio,
                "CLIENTE"
            )
        );

        assertEquals(
            "Has alcanzado el límite máximo de reprogramaciones para este turno. Comunícate con el local.",
            ex.getMessage()
        );
    }

    @Test
    @DisplayName("testReprogramacionExcluyeTurnoOriginalDeColisiones")
    void testReprogramacionExcluyeTurnoOriginalDeColisiones() {
        Turno turnoOriginal = construirTurnoBase(EstadoTurno.CONFIRMADO, 0);
        PerfilProfesional profesionalDestino = turnoOriginal.getProfesional();
        LocalDate nuevaFecha = LocalDate.now().plusDays(3);
        LocalTime nuevaHoraInicio = LocalTime.of(12, 0);

        prepararStubsHappyPath(turnoOriginal, profesionalDestino);

        invocarAplicarReprogramacionSegura(
            turnoOriginal,
            profesionalDestino,
            nuevaFecha,
            nuevaHoraInicio,
            "PROFESIONAL"
        );

        verify(repositorioTurno).existeSolapamientoExcluyendo(
            eq(profesionalDestino),
            eq(nuevaFecha),
            eq(nuevaHoraInicio),
            eq(nuevaHoraInicio.plusMinutes(turnoOriginal.getDuracionMinutos() + turnoOriginal.getBufferMinutos())),
            eq(turnoOriginal.getId()),
            anyList()
        );
    }

    private void prepararStubsHappyPath(Turno turnoOriginal, PerfilProfesional profesionalDestino) {
        when(repositorioBloqueoFecha.findBloqueoEnFecha(eq(profesionalDestino), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        DisponibilidadProfesional disponibilidad = new DisponibilidadProfesional();
        disponibilidad.setHoraInicio(LocalTime.of(8, 0));
        disponibilidad.setHoraFin(LocalTime.of(20, 0));

        when(repositorioDisponibilidadProfesional.findByProfesionalAndDiaSemanaAndActivoTrue(
            eq(profesionalDestino),
            any()
        )).thenReturn(Collections.singletonList(disponibilidad));

        when(repositorioTurno.findIdsTurnosOcupantesSolapadosForUpdate(
            eq(profesionalDestino),
            any(LocalDate.class),
            any(LocalTime.class),
            any(LocalTime.class),
            eq(turnoOriginal.getId()),
            anyList()
        )).thenReturn(Collections.emptyList());

        when(repositorioTurno.existeSolapamientoExcluyendo(
            eq(profesionalDestino),
            any(LocalDate.class),
            any(LocalTime.class),
            any(LocalTime.class),
            eq(turnoOriginal.getId()),
            anyList()
        )).thenReturn(false);

        when(repositorioTurno.save(any(Turno.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repositorioPago.findByTurnoId(anyLong())).thenReturn(Optional.empty());
    }

    private Turno construirTurnoBase(EstadoTurno estado, int cantidadReprogramacionesCliente) {
        Empresa empresa = new Empresa();
        empresa.setId(100L);
        empresa.setTimezone("America/Argentina/Buenos_Aires");

        PerfilProfesional profesional = new PerfilProfesional();
        profesional.setId(200L);
        profesional.setEmpresa(empresa);
        profesional.setActivo(true);

        Servicio servicio = new Servicio();
        servicio.setId(300L);
        servicio.setNombre("Servicio Test");

        Cliente cliente = new Cliente();
        cliente.setId(400L);

        Turno turno = new Turno();
        turno.setId(1L);
        turno.setEmpresa(empresa);
        turno.setProfesional(profesional);
        turno.setServicio(servicio);
        turno.setCliente(cliente);
        turno.setFecha(LocalDate.now().plusDays(1));
        turno.setHoraInicio(LocalTime.of(9, 0));
        turno.setHoraFin(LocalTime.of(9, 45));
        turno.setDuracionMinutos(30);
        turno.setBufferMinutos(15);
        turno.setPrecio(BigDecimal.valueOf(10000));
        turno.setEstado(estado);
        turno.setCantidadReprogramacionesCliente(cantidadReprogramacionesCliente);

        return turno;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Turno invocarAplicarReprogramacionSegura(
        Turno turnoOriginal,
        PerfilProfesional profesionalDestino,
        LocalDate nuevaFecha,
        LocalTime nuevaHoraInicio,
        String origen
    ) {
        Class<?> origenEnumClass = Arrays.stream(ServicioTurno.class.getDeclaredClasses())
            .filter(inner -> inner.getSimpleName().equals("OrigenReprogramacion"))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No se encontró OrigenReprogramacion"));

        Object origenEnum = Enum.valueOf((Class<? extends Enum>) origenEnumClass.asSubclass(Enum.class), origen);

        return ReflectionTestUtils.invokeMethod(
            servicioTurno,
            "aplicarReprogramacionSegura",
            turnoOriginal,
            profesionalDestino,
            nuevaFecha,
            nuevaHoraInicio,
            origenEnum
        );
    }
}
