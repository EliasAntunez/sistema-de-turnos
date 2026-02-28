package com.example.sitema_de_turnos.servicio.scheduler;

import com.example.sitema_de_turnos.modelo.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Builder de datos de prueba para tests
 */
public class TestDataBuilder {

    public static Turno crearTurnoConfirmado() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNombre("Test Empresa");
        empresa.setTelefono("+54911"+ "12345678");
        empresa.setDireccion("Av. Test 123");
        empresa.setCiudad("Buenos Aires");
        empresa.setProvincia("Buenos Aires");
        empresa.setTimezone("America/Argentina/Buenos_Aires");

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Juan Pérez");
        cliente.setEmail("juan@example.com");
        cliente.setTelefono("+5491123456789");
        cliente.setEmpresa(empresa);

        Profesional profesional = new Profesional();
        profesional.setId(1L);
        profesional.setNombre("Dr. López");
        profesional.setEmail("lopez@example.com");
        profesional.setEmpresa(empresa);

        Servicio servicio = new Servicio();
        servicio.setId(1L);
        servicio.setNombre("Consulta General");
        servicio.setDuracionMinutos(30);
        servicio.setEmpresa(empresa);

        Turno turno = new Turno();
        turno.setId(1L);
        turno.setEmpresa(empresa);
        turno.setCliente(cliente);
        turno.setProfesional(profesional);
        turno.setServicio(servicio);
        turno.setFecha(LocalDate.now().plusDays(1));
        turno.setHoraInicio(LocalTime.of(10, 0));
        turno.setHoraFin(LocalTime.of(10, 30));
        turno.setEstado(EstadoTurno.CONFIRMADO);
        turno.setRecordatorioEnviado(false);
        turno.setRecordatorioIntentos(0);

        return turno;
    }

    public static Turno crearTurnoCancelado() {
        Turno turno = crearTurnoConfirmado();
        turno.setEstado(EstadoTurno.CANCELADO);
        return turno;
    }

    public static Turno crearTurnoCompletado() {
        Turno turno = crearTurnoConfirmado();
        turno.setEstado(EstadoTurno.ATENDIDO);
        return turno;
    }
}
