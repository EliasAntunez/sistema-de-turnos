package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.publico.ProfesionalPublicoResponse;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.PerfilProfesional;
import com.example.sitema_de_turnos.modelo.ProfesionalServicio;
import com.example.sitema_de_turnos.modelo.Servicio;
import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.repositorio.RepositorioBloqueoFecha;
import com.example.sitema_de_turnos.repositorio.RepositorioDisponibilidadProfesional;
import com.example.sitema_de_turnos.repositorio.RepositorioEmpresa;
import com.example.sitema_de_turnos.repositorio.RepositorioHorarioEmpresa;
import com.example.sitema_de_turnos.repositorio.RepositorioPerfilProfesional;
import com.example.sitema_de_turnos.repositorio.RepositorioProfesionalServicio;
import com.example.sitema_de_turnos.repositorio.RepositorioServicio;
import com.example.sitema_de_turnos.repositorio.RepositorioTurno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ServicioPublico.
 * Cubre la lógica de obtenerProfesionalesPorServicio() con el modelo whitelist.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ServicioPublico - obtenerProfesionalesPorServicio Whitelist")
class ServicioPublicoWhitelistTest {

    @Mock private ServicioPoliticaCancelacion servicioPoliticaCancelacion;
    @Mock private RepositorioEmpresa repositorioEmpresa;
    @Mock private RepositorioServicio repositorioServicio;
    @Mock private RepositorioPerfilProfesional repositorioPerfilProfesional;
    @Mock private RepositorioProfesionalServicio repositorioProfesionalServicio;
    @Mock private RepositorioDisponibilidadProfesional repositorioDisponibilidad;
    @Mock private RepositorioHorarioEmpresa repositorioHorarioEmpresa;
    @Mock private RepositorioBloqueoFecha repositorioBloqueoFecha;
    @Mock private RepositorioTurno repositorioTurno;

    @InjectMocks
    private ServicioPublico servicioPublico;

    private Empresa empresa;
    private Servicio servicio;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNombre("Peluquería Test");
        empresa.setActiva(true);

        servicio = new Servicio();
        servicio.setId(10L);
        servicio.setNombre("Corte");
        servicio.setDuracionMinutos(30);
        servicio.setPrecio(BigDecimal.valueOf(500));
        servicio.setActivo(true);
        servicio.setEmpresa(empresa);

        when(repositorioEmpresa.findBySlugAndActivaTrue("test-slug")).thenReturn(Optional.of(empresa));
        when(repositorioServicio.findById(10L)).thenReturn(Optional.of(servicio));
    }

    private PerfilProfesional crearProfesional(Long id, boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Profesional " + id);
        usuario.setApellido("Apellido");

        PerfilProfesional p = new PerfilProfesional();
        p.setId(id);
        p.setUsuario(usuario);
        p.setActivo(activo);
        p.setEmpresa(empresa);
        return p;
    }

    private ProfesionalServicio crearHabilitacion(PerfilProfesional profesional, Servicio srv) {
        ProfesionalServicio ps = new ProfesionalServicio();
        ps.setId(100L + profesional.getId());
        ps.setProfesional(profesional);
        ps.setServicio(srv);
        ps.setActivo(true);
        return ps;
    }

    @Test
    @DisplayName("Profesional activo con habilitación → aparece en la lista")
    void obtenerProfesionalesPorServicio_profesionalActivoConHabilitacion_apareceEnLista() {
        PerfilProfesional profesionalActivo = crearProfesional(1L, true);
        ProfesionalServicio habilitacion = crearHabilitacion(profesionalActivo, servicio);

        when(repositorioPerfilProfesional.findByEmpresaAndActivoTrue(empresa)).thenReturn(List.of(profesionalActivo));
        when(repositorioProfesionalServicio.findByProfesionalAndActivoTrue(profesionalActivo))
                .thenReturn(List.of(habilitacion));

        List<ProfesionalPublicoResponse> resultado = servicioPublico.obtenerProfesionalesPorServicio("test-slug", 10L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
    }

    @Test
    @DisplayName("Profesional inactivo (activo=false) con habilitación → NO aparece en la lista")
    void obtenerProfesionalesPorServicio_profesionalInactivo_noApareceEnLista() {
        PerfilProfesional profesionalInactivo = crearProfesional(2L, false);
        ProfesionalServicio habilitacion = crearHabilitacion(profesionalInactivo, servicio);

        when(repositorioPerfilProfesional.findByEmpresaAndActivoTrue(empresa)).thenReturn(Collections.emptyList());
        // No se llama a findByProfesionalAndActivoTrue porque findByEmpresaAndActivoTrue ya filtra inactivos

        List<ProfesionalPublicoResponse> resultado = servicioPublico.obtenerProfesionalesPorServicio("test-slug", 10L);

        assertTrue(resultado.isEmpty(), "Un profesional inactivo no debe aparecer aunque tenga habilitación");
        verify(repositorioProfesionalServicio, never()).findByProfesionalAndActivoTrue(profesionalInactivo);
    }

    @Test
    @DisplayName("Profesional activo sin registro en profesional_servicio → NO aparece en la lista")
    void obtenerProfesionalesPorServicio_profesionalActivoSinHabilitacion_noApareceEnLista() {
        PerfilProfesional profesionalSinHabilitacion = crearProfesional(3L, true);

        when(repositorioPerfilProfesional.findByEmpresaAndActivoTrue(empresa)).thenReturn(List.of(profesionalSinHabilitacion));
        when(repositorioProfesionalServicio.findByProfesionalAndActivoTrue(profesionalSinHabilitacion))
                .thenReturn(Collections.emptyList());

        List<ProfesionalPublicoResponse> resultado = servicioPublico.obtenerProfesionalesPorServicio("test-slug", 10L);

        assertTrue(resultado.isEmpty(),
                "Un profesional activo sin habilitación en profesional_servicio no debe aparecer");
    }

    // ===== Bloque C - edge cases =====

    @Test
    @DisplayName("[C3] Profesional con habilitación para OTRO servicio → NO aparece para el servicio consultado")
    void obtenerProfesionalesPorServicio_profesionalConHabilitacionParaOtroServicio_noAparece() {
        // Profesional activo pero su PS record es para el servicio 99, no para el 10 que se consulta
        PerfilProfesional profesional = crearProfesional(4L, true);

        Servicio otroServicio = new Servicio();
        otroServicio.setId(99L);

        ProfesionalServicio psParaOtroServicio = new ProfesionalServicio();
        psParaOtroServicio.setId(400L);
        psParaOtroServicio.setProfesional(profesional);
        psParaOtroServicio.setServicio(otroServicio);
        psParaOtroServicio.setActivo(true);

        when(repositorioPerfilProfesional.findByEmpresaAndActivoTrue(empresa)).thenReturn(List.of(profesional));
        when(repositorioProfesionalServicio.findByProfesionalAndActivoTrue(profesional))
                .thenReturn(List.of(psParaOtroServicio)); // список не пустой, но не совпадает с id=10

        List<ProfesionalPublicoResponse> resultado = servicioPublico.obtenerProfesionalesPorServicio("test-slug", 10L);

        assertTrue(resultado.isEmpty(),
                "Un profesional habilitado para el servicio 99 no debe aparecer en el listado del servicio 10");
    }
}
