package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.ProfesionalServicioResponse;
import com.example.sitema_de_turnos.modelo.Dueno;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.Profesional;
import com.example.sitema_de_turnos.modelo.ProfesionalServicio;
import com.example.sitema_de_turnos.modelo.Servicio;
import com.example.sitema_de_turnos.repositorio.RepositorioProfesional;
import com.example.sitema_de_turnos.repositorio.RepositorioProfesionalServicio;
import com.example.sitema_de_turnos.repositorio.RepositorioServicio;
import com.example.sitema_de_turnos.repositorio.RepositorioTurno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ServicioProfesional.
 * Cubre: obtenerServiciosDeProfesional() y toggleServicioProfesional().
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ServicioProfesional - Whitelist Tests")
class ServicioProfesionalWhitelistTest {

    @Mock private RepositorioProfesional repositorioProfesional;
    @Mock private RepositorioProfesionalServicio repositorioProfesionalServicio;
    @Mock private RepositorioServicio repositorioServicio;
    @Mock private RepositorioTurno repositorioTurno;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ServicioValidacionDueno servicioValidacionDueno;

    @InjectMocks
    private ServicioProfesional servicioProfesional;

    private Empresa empresa;
    private Dueno dueno;
    private Profesional profesional;
    private Servicio servicioActivo;

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

        profesional = new Profesional();
        profesional.setId(10L);
        profesional.setNombre("Pedro");
        profesional.setApellido("López");
        profesional.setActivo(true);
        profesional.setEmpresa(empresa);

        servicioActivo = new Servicio();
        servicioActivo.setId(20L);
        servicioActivo.setNombre("Corte");
        servicioActivo.setDuracionMinutos(30);
        servicioActivo.setPrecio(BigDecimal.valueOf(500));
        servicioActivo.setActivo(true);
        servicioActivo.setEmpresa(empresa);
    }

    // ==================== obtenerServiciosDeProfesional ====================

    @Test
    @DisplayName("obtenerServiciosDeProfesional → solo usa findByEmpresaAndActivoTrue (filtra inactivos)")
    void obtenerServiciosDeProfesional_usaQueryActivoTrue_excluyeServiciosInactivos() {
        when(servicioValidacionDueno.validarYObtenerDueno("dueno@test.com")).thenReturn(dueno);
        when(repositorioProfesional.findById(10L)).thenReturn(Optional.of(profesional));
        when(repositorioServicio.findByEmpresaAndActivoTrue(empresa)).thenReturn(List.of(servicioActivo));

        ProfesionalServicio habilitacion = new ProfesionalServicio();
        habilitacion.setId(1L);
        habilitacion.setProfesional(profesional);
        habilitacion.setServicio(servicioActivo);
        habilitacion.setActivo(true);

        when(repositorioProfesionalServicio.findByProfesionalAndActivoTrue(profesional))
                .thenReturn(List.of(habilitacion));

        List<ProfesionalServicioResponse> resultado =
                servicioProfesional.obtenerServiciosDeProfesional("dueno@test.com", 10L);

        // Verifica que se usa el query con filtro activo=true (no findByEmpresa sin filtro)
        verify(repositorioServicio).findByEmpresaAndActivoTrue(empresa);
        verify(repositorioServicio, never()).findByEmpresa(any());

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getDisponible(),
                "El servicio habilitado debe aparecer con disponible=true");
    }

    // ==================== toggleServicioProfesional ====================

    @Test
    @DisplayName("Toggle habilitar → crea ProfesionalServicio con activo=true cuando no existía")
    void toggleServicioProfesional_habilitar_creaRegistroConActivoTrue() {
        when(servicioValidacionDueno.validarYObtenerDueno("dueno@test.com")).thenReturn(dueno);
        when(repositorioProfesional.findById(10L)).thenReturn(Optional.of(profesional));
        when(repositorioServicio.findById(20L)).thenReturn(Optional.of(servicioActivo));
        when(repositorioProfesionalServicio.findByProfesionalAndServicio(profesional, servicioActivo))
                .thenReturn(Optional.empty());

        servicioProfesional.toggleServicioProfesional("dueno@test.com", 10L, 20L, true);

        ArgumentCaptor<ProfesionalServicio> captor = ArgumentCaptor.forClass(ProfesionalServicio.class);
        verify(repositorioProfesionalServicio, times(1)).save(captor.capture());

        ProfesionalServicio ps = captor.getValue();
        assertEquals(profesional, ps.getProfesional());
        assertEquals(servicioActivo, ps.getServicio());
        assertTrue(ps.getActivo(), "El registro creado debe tener activo=true");
    }

    @Test
    @DisplayName("Toggle deshabilitar → elimina el registro existente de profesional_servicio")
    void toggleServicioProfesional_deshabilitar_eliminaRegistroExistente() {
        ProfesionalServicio existente = new ProfesionalServicio();
        existente.setId(99L);
        existente.setProfesional(profesional);
        existente.setServicio(servicioActivo);
        existente.setActivo(true);

        when(servicioValidacionDueno.validarYObtenerDueno("dueno@test.com")).thenReturn(dueno);
        when(repositorioProfesional.findById(10L)).thenReturn(Optional.of(profesional));
        when(repositorioServicio.findById(20L)).thenReturn(Optional.of(servicioActivo));
        when(repositorioProfesionalServicio.findByProfesionalAndServicio(profesional, servicioActivo))
                .thenReturn(Optional.of(existente));

        servicioProfesional.toggleServicioProfesional("dueno@test.com", 10L, 20L, false);

        verify(repositorioProfesionalServicio, times(1)).delete(existente);
        verify(repositorioProfesionalServicio, never()).save(any());
    }

    // ===== Bloque C - edge cases =====

    @Test
    @DisplayName("[C2] Toggle habilitar idempotente → si ya existe registro activo, lo actualiza sin crear duplicado")
    void toggleServicioProfesional_habilitarIdempotente_noCreaDuplicado() {
        // Registro ya existente y activo (p.ej.: auto-onboarding previo)
        ProfesionalServicio existente = new ProfesionalServicio();
        existente.setId(50L);
        existente.setProfesional(profesional);
        existente.setServicio(servicioActivo);
        existente.setActivo(true);

        when(servicioValidacionDueno.validarYObtenerDueno("dueno@test.com")).thenReturn(dueno);
        when(repositorioProfesional.findById(10L)).thenReturn(Optional.of(profesional));
        when(repositorioServicio.findById(20L)).thenReturn(Optional.of(servicioActivo));
        when(repositorioProfesionalServicio.findByProfesionalAndServicio(profesional, servicioActivo))
                .thenReturn(Optional.of(existente));

        servicioProfesional.toggleServicioProfesional("dueno@test.com", 10L, 20L, true);

        // Debe actualizar el registro existente, nunca crear uno nuevo
        ArgumentCaptor<ProfesionalServicio> captor = ArgumentCaptor.forClass(ProfesionalServicio.class);
        verify(repositorioProfesionalServicio, times(1)).save(captor.capture());
        assertEquals(50L, captor.getValue().getId(),
                "Debe guardar el registro existente (id=50), no crear uno nuevo");
        assertTrue(captor.getValue().getActivo(), "El registro debe seguir con activo=true");
        verify(repositorioProfesionalServicio, never()).delete(any());
    }
}
