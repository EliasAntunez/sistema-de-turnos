/*

package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.RegistroServicioRequest;
import com.example.sitema_de_turnos.modelo.Dueno;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.Profesional;
import com.example.sitema_de_turnos.modelo.ProfesionalServicio;
import com.example.sitema_de_turnos.modelo.Servicio;
import com.example.sitema_de_turnos.repositorio.RepositorioProfesionalServicio;
import com.example.sitema_de_turnos.repositorio.RepositorioServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ServicioServicio.
 * Cubre la lógica de auto-onboarding en crearServicio().
 */

/*
@ExtendWith(MockitoExtension.class)
@DisplayName("ServicioServicio - Auto-onboarding en crearServicio")
class ServicioServicioTest {

    @Mock
    private RepositorioServicio repositorioServicio;

    @Mock
    private RepositorioProfesionalServicio repositorioProfesionalServicio;

    @Mock
    private ServicioValidacionDueno servicioValidacionDueno;

    @InjectMocks
    private ServicioServicio servicioServicio;

    private Empresa empresa;
    private Dueno dueno;
    private RegistroServicioRequest request;
    private Servicio servicioGuardado;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNombre("Peluquería Test");
        empresa.setActiva(true);

        dueno = new Dueno();
        dueno.setId(1L);
        dueno.setEmail("dueno@test.com");
        dueno.setActivo(true);
        dueno.setEmpresa(empresa);

        request = new RegistroServicioRequest();
        request.setNombre("Corte");
        request.setDuracionMinutos(30);
        request.setPrecio(BigDecimal.valueOf(500));

        servicioGuardado = new Servicio();
        servicioGuardado.setId(10L);
        servicioGuardado.setNombre("Corte");
        servicioGuardado.setDuracionMinutos(30);
        servicioGuardado.setPrecio(BigDecimal.valueOf(500));
        servicioGuardado.setActivo(true);
        servicioGuardado.setEmpresa(empresa);
        servicioGuardado.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    @DisplayName("Empresa con 1 profesional activo → crea ProfesionalServicio con activo=true")
    void crearServicio_uniPersonalConProfesionalActivo_creaRegistroProfesionalServicioActivo() {
        Profesional profesionalActivo = new Profesional();
        profesionalActivo.setId(1L);
        profesionalActivo.setActivo(true);
        empresa.getProfesionales().add(profesionalActivo);

        when(servicioValidacionDueno.validarYObtenerDueno("dueno@test.com")).thenReturn(dueno);
        when(repositorioServicio.save(any(Servicio.class))).thenReturn(servicioGuardado);

        servicioServicio.crearServicio("dueno@test.com", request);

        ArgumentCaptor<ProfesionalServicio> captor = ArgumentCaptor.forClass(ProfesionalServicio.class);
        verify(repositorioProfesionalServicio, times(1)).save(captor.capture());

        ProfesionalServicio ps = captor.getValue();
        assertEquals(profesionalActivo, ps.getProfesional());
        assertEquals(servicioGuardado, ps.getServicio());
        assertTrue(ps.getActivo(), "El registro debe crearse con activo=true");
    }

    @Test
    @DisplayName("Empresa con 1 profesional inactivo → NO crea ProfesionalServicio")
    void crearServicio_uniPersonalConProfesionalInactivo_noCreaRegistroProfesionalServicio() {
        Profesional profesionalInactivo = new Profesional();
        profesionalInactivo.setId(1L);
        profesionalInactivo.setActivo(false);
        empresa.getProfesionales().add(profesionalInactivo);

        when(servicioValidacionDueno.validarYObtenerDueno("dueno@test.com")).thenReturn(dueno);
        when(repositorioServicio.save(any(Servicio.class))).thenReturn(servicioGuardado);

        servicioServicio.crearServicio("dueno@test.com", request);

        verify(repositorioProfesionalServicio, never()).save(any());
    }

    @Test
    @DisplayName("Empresa multi-profesional (2+ activos) → NO crea ProfesionalServicio automáticamente")
    void crearServicio_multiProfesionalActivos_noCreaRegistroProfesionalServicioAutomaticamente() {
        Profesional p1 = new Profesional();
        p1.setId(1L);
        p1.setActivo(true);

        Profesional p2 = new Profesional();
        p2.setId(2L);
        p2.setActivo(true);

        empresa.getProfesionales().addAll(List.of(p1, p2));

        when(servicioValidacionDueno.validarYObtenerDueno("dueno@test.com")).thenReturn(dueno);
        when(repositorioServicio.save(any(Servicio.class))).thenReturn(servicioGuardado);

        servicioServicio.crearServicio("dueno@test.com", request);

        verify(repositorioProfesionalServicio, never()).save(any());
    }

    // ===== Bloque C - edge cases =====

    @Test
    @DisplayName("[C1] Empresa sin ningún profesional → NO crea ningún registro ProfesionalServicio")
    void crearServicio_empresaSinProfesionales_noCreaRegistro() {
        // empresa.getProfesionales() está vacío por defecto en setUp()
        when(servicioValidacionDueno.validarYObtenerDueno("dueno@test.com")).thenReturn(dueno);
        when(repositorioServicio.save(any(Servicio.class))).thenReturn(servicioGuardado);

        servicioServicio.crearServicio("dueno@test.com", request);

        verify(repositorioProfesionalServicio, never()).save(any());
    }
}

 */