package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.WinBackConfigDto;
import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;
import com.example.sitema_de_turnos.modelo.BotConfiguracion;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.PerfilDueno;
import com.example.sitema_de_turnos.repositorio.RepositorioBotConfiguracion;
import com.example.sitema_de_turnos.repositorio.RepositorioEmpresa;
import com.example.sitema_de_turnos.repositorio.RepositorioPerfilDueno;
import com.example.sitema_de_turnos.repositorio.RepositorioPerfilProfesional;
import com.example.sitema_de_turnos.repositorio.RepositorioUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServicioEmpresa Win-Back Upselling / Feature Discovery Tests")
class ServicioEmpresaWinBackTest {

    @Mock private RepositorioEmpresa repositorioEmpresa;
    @Mock private RepositorioPerfilDueno repositorioPerfilDueno;
    @Mock private RepositorioPerfilProfesional repositorioPerfilProfesional;
    @Mock private RepositorioUsuario repositorioUsuario;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ServicioDueno servicioDueno;
    @Mock private RepositorioBotConfiguracion repositorioBotConfiguracion;

    private ServicioEmpresa servicioEmpresa;
    private PerfilDueno perfilDueno;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        servicioEmpresa = new ServicioEmpresa(
            repositorioEmpresa,
            repositorioPerfilDueno,
            repositorioPerfilProfesional,
            repositorioUsuario,
            passwordEncoder,
            servicioDueno,
            repositorioBotConfiguracion
        );

        perfilDueno = new PerfilDueno();
        perfilDueno.setId(1L);

        empresa = new Empresa();
        empresa.setId(1L);
        empresa.setWinBackHabilitado(false);
        empresa.setWinBackDiasInactividad(30);
        empresa.setWinBackDiasEsperaReenvio(60);
        empresa.setWinBackMensajePlantilla("Test message");
        empresa.setWinBackDescuentoPorcentaje(10);
    }

    @Test
    @DisplayName("Debe retornar botInactivo = true si no existe configuración de bot")
    void debeRetornarBotInactivoTrueSiNoExisteBotConfig() {
        when(servicioDueno.obtenerPorEmail("dueno@test.com")).thenReturn(perfilDueno);
        when(repositorioEmpresa.findByPerfilDuenoId(1L)).thenReturn(Optional.of(empresa));
        when(repositorioBotConfiguracion.findByTenantId(1L)).thenReturn(Optional.empty());

        WinBackConfigDto response = servicioEmpresa.obtenerConfiguracionWinBack("dueno@test.com");

        assertNotNull(response);
        assertTrue(response.getBotInactivo());
    }

    @Test
    @DisplayName("Debe retornar botInactivo = true si el bot está desactivado")
    void debeRetornarBotInactivoTrueSiElBotEstaDesactivado() {
        BotConfiguracion botConfig = new BotConfiguracion();
        botConfig.setTenantId(1L);
        botConfig.setEstadoBot(false);
        botConfig.setInstanciaWhatsapp("test-instance");

        when(servicioDueno.obtenerPorEmail("dueno@test.com")).thenReturn(perfilDueno);
        when(repositorioEmpresa.findByPerfilDuenoId(1L)).thenReturn(Optional.of(empresa));
        when(repositorioBotConfiguracion.findByTenantId(1L)).thenReturn(Optional.of(botConfig));

        WinBackConfigDto response = servicioEmpresa.obtenerConfiguracionWinBack("dueno@test.com");

        assertNotNull(response);
        assertTrue(response.getBotInactivo());
    }

    @Test
    @DisplayName("Debe retornar botInactivo = false si el bot está activo y con instancia válida")
    void debeRetornarBotInactivoFalseSiElBotEstaActivoYValido() {
        BotConfiguracion botConfig = new BotConfiguracion();
        botConfig.setTenantId(1L);
        botConfig.setEstadoBot(true);
        botConfig.setInstanciaWhatsapp("test-instance");

        when(servicioDueno.obtenerPorEmail("dueno@test.com")).thenReturn(perfilDueno);
        when(repositorioEmpresa.findByPerfilDuenoId(1L)).thenReturn(Optional.of(empresa));
        when(repositorioBotConfiguracion.findByTenantId(1L)).thenReturn(Optional.of(botConfig));

        WinBackConfigDto response = servicioEmpresa.obtenerConfiguracionWinBack("dueno@test.com");

        assertNotNull(response);
        assertFalse(response.getBotInactivo());
    }

    @Test
    @DisplayName("Debe lanzar AccesoDenegadoException al intentar actualizar con bot inactivo")
    void debeLanzarAccesoDenegadoAlActualizarConBotInactivo() {
        WinBackConfigDto requestDto = new WinBackConfigDto();
        requestDto.setWinBackHabilitado(true);

        when(servicioDueno.obtenerPorEmail("dueno@test.com")).thenReturn(perfilDueno);
        when(repositorioEmpresa.findByPerfilDuenoId(1L)).thenReturn(Optional.of(empresa));
        when(repositorioBotConfiguracion.findByTenantId(1L)).thenReturn(Optional.empty());

        assertThrows(AccesoDenegadoException.class, () -> {
            servicioEmpresa.actualizarConfiguracionWinBack("dueno@test.com", requestDto);
        });

        verify(repositorioEmpresa, never()).save(any(Empresa.class));
    }
}
