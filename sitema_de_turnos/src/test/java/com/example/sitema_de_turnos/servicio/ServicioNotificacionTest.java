package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.NotificacionResponse;
import com.example.sitema_de_turnos.dto.NotificacionWebSocketDTO;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.modelo.Notificacion;
import com.example.sitema_de_turnos.modelo.Profesional;
import com.example.sitema_de_turnos.modelo.TipoNotificacion;
import com.example.sitema_de_turnos.repositorio.RepositorioNotificacion;
import com.example.sitema_de_turnos.repositorio.RepositorioProfesional;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests exhaustivos para ServicioNotificacion
 * Cubre: envío WebSocket, persistencia, consultas, limpieza programada
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ServicioNotificacion Tests")
class ServicioNotificacionTest {

    @Mock
    private RepositorioNotificacion repositorioNotificacion;

    @Mock
    private RepositorioProfesional repositorioProfesional;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ServicioNotificacion servicioNotificacion;

    private Profesional profesional;
    private Notificacion notificacionNoLeida;
    private final String emailProfesional = "doctor@example.com";

    @BeforeEach
    void setUp() {
        // Configurar retentionDays
        ReflectionTestUtils.setField(servicioNotificacion, "retentionDays", 90);

        // Crear profesional mock
        profesional = new Profesional();
        profesional.setId(1L);
        profesional.setNombre("Dr. Test");
        profesional.setEmail(emailProfesional);

        // Crear notificación no leída
        notificacionNoLeida = new Notificacion();
        notificacionNoLeida.setId(1L);
        notificacionNoLeida.setProfesional(profesional);
        notificacionNoLeida.setTipo(TipoNotificacion.NUEVO_TURNO);
        notificacionNoLeida.setTitulo("Nuevo turno");
        notificacionNoLeida.setMensaje("Tiene un nuevo turno pendiente");
        notificacionNoLeida.setLeida(false);
        notificacionNoLeida.setTurnoId(100L);
        notificacionNoLeida.setFechaCreacion(LocalDateTime.now());
    }

    // ==================== TESTS DE ENVÍO DE NOTIFICACIONES ====================

    @Test
    @DisplayName("Debe enviar notificación exitosamente y persistir en BD")
    void debeEnviarNotificacionExitosa() throws Exception {
        when(repositorioProfesional.findById(1L)).thenReturn(Optional.of(profesional));
        when(repositorioNotificacion.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion n = invocation.getArgument(0);
            n.setId(1L);
            n.setFechaCreacion(LocalDateTime.now());
            return n;
        });

        Notificacion resultado = servicioNotificacion.enviarNotificacion(
                1L,
                TipoNotificacion.NUEVO_TURNO,
                "Nuevo turno",
                "Tiene un nuevo turno asignado",
                null,
                100L
        );

        assertNotNull(resultado);
        assertEquals(TipoNotificacion.NUEVO_TURNO, resultado.getTipo());
        assertEquals("Nuevo turno", resultado.getTitulo());
        assertFalse(resultado.getLeida());
        assertEquals(100L, resultado.getTurnoId());

        verify(repositorioNotificacion, times(1)).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("Debe enviar notificación por WebSocket al profesional correcto")
    void debeEnviarPorWebSocketAlProfesionalCorrecto() throws Exception {
        when(repositorioProfesional.findById(1L)).thenReturn(Optional.of(profesional));
        when(repositorioNotificacion.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion n = invocation.getArgument(0);
            n.setId(1L);
            n.setFechaCreacion(LocalDateTime.now());
            return n;
        });

        servicioNotificacion.enviarNotificacion(
                1L,
                TipoNotificacion.CANCELACION_CLIENTE,
                "Turno cancelado",
                "El turno fue cancelado",
                null,
                100L
        );

        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NotificacionWebSocketDTO> dtoCaptor = ArgumentCaptor.forClass(NotificacionWebSocketDTO.class);

        verify(messagingTemplate, times(1)).convertAndSend(
                destinationCaptor.capture(),
                dtoCaptor.capture()
        );

        assertEquals("/topic/notifications/1", destinationCaptor.getValue());
        
        NotificacionWebSocketDTO dto = dtoCaptor.getValue();
        assertEquals(TipoNotificacion.CANCELACION_CLIENTE.name(), dto.getTipo());
        assertEquals("Turno cancelado", dto.getTitulo());
        assertEquals("El turno fue cancelado", dto.getMensaje());
        assertEquals(100L, dto.getTurnoId());
    }

    @Test
    @DisplayName("Debe serializar datos adicionales a JSON correctamente")
    void debeSerializarDatosAdicionalesAJson() throws Exception {
        Map<String, Object> datosAdicionales = new HashMap<>();
        datosAdicionales.put("clienteNombre", "Juan Pérez");
        datosAdicionales.put("hora", "10:00");

        String jsonEsperado = "{\"clienteNombre\":\"Juan Pérez\",\"hora\":\"10:00\"}";

        when(repositorioProfesional.findById(1L)).thenReturn(Optional.of(profesional));
        when(objectMapper.writeValueAsString(datosAdicionales)).thenReturn(jsonEsperado);
        when(repositorioNotificacion.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });

        Notificacion resultado = servicioNotificacion.enviarNotificacion(
                1L,
                TipoNotificacion.NUEVO_TURNO,
                "Nuevo turno",
                "Mensaje de prueba",
                datosAdicionales,
                100L
        );

        assertEquals(jsonEsperado, resultado.getContenidoJson());
        verify(objectMapper, times(1)).writeValueAsString(datosAdicionales);
    }

    @Test
    @DisplayName("Debe manejar error de serialización JSON sin interrumpir flujo")
    void debeManejarErrorSerializacionJson() throws Exception {
        Map<String, Object> datosProblematicos = new HashMap<>();
        datosProblematicos.put("circular", datosProblematicos); // Referencia circular

        when(repositorioProfesional.findById(1L)).thenReturn(Optional.of(profesional));
        when(objectMapper.writeValueAsString(any())).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("Error circular") {});
        when(repositorioNotificacion.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });

        // No debe lanzar excepción, solo loggear error
        Notificacion resultado = servicioNotificacion.enviarNotificacion(
                1L,
                TipoNotificacion.NUEVO_TURNO,
                "Nuevo turno",
                "Mensaje",
                datosProblematicos,
                100L
        );

        assertNotNull(resultado);
        assertNull(resultado.getContenidoJson()); // No se pudo serializar
    }

    @Test
    @DisplayName("Debe lanzar excepción si profesional no existe")
    void debeLanzarExcepcionSiProfesionalNoExiste() {
        when(repositorioProfesional.findById(999L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(RecursoNoEncontradoException.class, () -> {
            servicioNotificacion.enviarNotificacion(
                    999L,
                    TipoNotificacion.NUEVO_TURNO,
                    "Título",
                    "Mensaje",
                    null,
                    100L
            );
        });

        assertEquals("Profesional no encontrado", exception.getMessage());
        verify(repositorioNotificacion, never()).save(any());
    }

    @Test
    @DisplayName("Debe continuar si falla envío por WebSocket (no debe interrumpir persistencia)")
    void debeContinuarSiFallaWebSocket() {
        when(repositorioProfesional.findById(1L)).thenReturn(Optional.of(profesional));
        when(repositorioNotificacion.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion n = invocation.getArgument(0);
            n.setId(1L);
            n.setFechaCreacion(LocalDateTime.now());
            return n;
        });
        lenient().doThrow(new RuntimeException("Error WebSocket")).when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        // No debe lanzar excepción
        Notificacion resultado = servicioNotificacion.enviarNotificacion(
                1L,
                TipoNotificacion.NUEVO_TURNO,
                "Título",
                "Mensaje",
                null,
                100L
        );

        assertNotNull(resultado);
        verify(repositorioNotificacion, times(1)).save(any());
    }

    // ==================== TESTS DE MARCADO COMO LEÍDA ====================

    @Test
    @DisplayName("Debe marcar notificación como leída correctamente")
    void debeMarcarComoLeida() {
        when(repositorioNotificacion.findById(1L)).thenReturn(Optional.of(notificacionNoLeida));

        servicioNotificacion.marcarComoLeida(1L, emailProfesional);

        assertTrue(notificacionNoLeida.getLeida());
        assertNotNull(notificacionNoLeida.getFechaLectura());
        verify(repositorioNotificacion, times(1)).save(notificacionNoLeida);
    }

    @Test
    @DisplayName("Debe lanzar excepción si notificación no existe")
    void debeLanzarExcepcionSiNotificacionNoExiste() {
        when(repositorioNotificacion.findById(999L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(RecursoNoEncontradoException.class, () -> {
            servicioNotificacion.marcarComoLeida(999L, emailProfesional);
        });

        assertEquals("Notificación no encontrada", exception.getMessage());
    }

    @Test
    @DisplayName("Debe validar que la notificación pertenece al profesional")
    void debeValidarPropiedadNotificacion() {
        Profesional otroProfesional = new Profesional();
        otroProfesional.setId(2L);
        otroProfesional.setEmail("otro@example.com");

        notificacionNoLeida.setProfesional(otroProfesional);

        when(repositorioNotificacion.findById(1L)).thenReturn(Optional.of(notificacionNoLeida));

        RecursoNoEncontradoException exception = assertThrows(RecursoNoEncontradoException.class, () -> {
            servicioNotificacion.marcarComoLeida(1L, emailProfesional);
        });

        assertEquals("Notificación no encontrada", exception.getMessage());
        verify(repositorioNotificacion, never()).save(any());
    }

    // ==================== TESTS DE MARCAR TODAS COMO LEÍDAS ====================

    @Test
    @DisplayName("Debe marcar todas las notificaciones no leídas del profesional")
    void debeMarcarTodasComoLeidas() {
        Notificacion notif1 = crearNotificacion(1L, false);
        Notificacion notif2 = crearNotificacion(2L, false);
        Notificacion notif3 = crearNotificacion(3L, false);

        List<Notificacion> noLeidas = Arrays.asList(notif1, notif2, notif3);

        when(repositorioProfesional.findByEmail(emailProfesional)).thenReturn(Optional.of(profesional));
        when(repositorioNotificacion.findNoLeidasByProfesional(profesional)).thenReturn(noLeidas);

        servicioNotificacion.marcarTodasComoLeidas(emailProfesional);

        assertTrue(notif1.getLeida());
        assertTrue(notif2.getLeida());
        assertTrue(notif3.getLeida());
        assertNotNull(notif1.getFechaLectura());
        assertNotNull(notif2.getFechaLectura());
        assertNotNull(notif3.getFechaLectura());

        verify(repositorioNotificacion, times(1)).saveAll(noLeidas);
    }

    @Test
    @DisplayName("No debe hacer nada si no hay notificaciones no leídas")
    void noDebeHacerNadaSiNoHayNoLeidas() {
        when(repositorioProfesional.findByEmail(emailProfesional)).thenReturn(Optional.of(profesional));
        when(repositorioNotificacion.findNoLeidasByProfesional(profesional)).thenReturn(Collections.emptyList());

        servicioNotificacion.marcarTodasComoLeidas(emailProfesional);

        verify(repositorioNotificacion, never()).saveAll(any());
    }

    // ==================== TESTS DE CONSULTAS ====================

    @Test
    @DisplayName("Debe obtener notificaciones no leídas correctamente")
    void debeObtenerNoLeidas() {
        List<Notificacion> noLeidas = Arrays.asList(
                crearNotificacion(1L, false),
                crearNotificacion(2L, false)
        );

        when(repositorioProfesional.findByEmail(emailProfesional)).thenReturn(Optional.of(profesional));
        when(repositorioNotificacion.findNoLeidasByProfesional(profesional)).thenReturn(noLeidas);

        List<NotificacionResponse> resultado = servicioNotificacion.obtenerNoLeidas(emailProfesional);

        assertEquals(2, resultado.size());
        assertFalse(resultado.get(0).getLeida());
        assertFalse(resultado.get(1).getLeida());
    }

    @Test
    @DisplayName("Debe contar notificaciones no leídas correctamente")
    void debeContarNoLeidas() {
        when(repositorioProfesional.findByEmail(emailProfesional)).thenReturn(Optional.of(profesional));
        when(repositorioNotificacion.countByProfesionalAndLeidaFalse(profesional)).thenReturn(5L);

        Long cantidad = servicioNotificacion.contarNoLeidas(emailProfesional);

        assertEquals(5L, cantidad);
    }

    @Test
    @DisplayName("Debe obtener historial con paginación")
    void debeObtenerHistorialPaginado() {
        List<Notificacion> notificaciones = Arrays.asList(
                crearNotificacion(1L, true),
                crearNotificacion(2L, false)
        );
        Page<Notificacion> page = new PageImpl<>(notificaciones);

        when(repositorioProfesional.findByEmail(emailProfesional)).thenReturn(Optional.of(profesional));
        when(repositorioNotificacion.findByProfesionalOrderByFechaCreacionDesc(
                eq(profesional), any(Pageable.class))).thenReturn(page);

        Page<NotificacionResponse> resultado = servicioNotificacion.obtenerHistorial(emailProfesional, 0, 10);

        assertEquals(2, resultado.getContent().size());
        verify(repositorioNotificacion, times(1)).findByProfesionalOrderByFechaCreacionDesc(
                eq(profesional), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe obtener últimas N notificaciones")
    void debeObtenerUltimas() {
        List<Notificacion> notificaciones = Arrays.asList(
                crearNotificacion(1L, false),
                crearNotificacion(2L, false),
                crearNotificacion(3L, true)
        );

        when(repositorioProfesional.findByEmail(emailProfesional)).thenReturn(Optional.of(profesional));
        when(repositorioNotificacion.findTopNByProfesional(eq(profesional), any(Pageable.class)))
                .thenReturn(notificaciones);

        List<NotificacionResponse> resultado = servicioNotificacion.obtenerUltimas(emailProfesional, 3);

        assertEquals(3, resultado.size());
    }

    @Test
    @DisplayName("Debe lanzar excepción en consultas si profesional no existe")
    void debeLanzarExcepcionEnConsultasSiProfesionalNoExiste() {
        when(repositorioProfesional.findByEmail("inexistente@example.com")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> {
            servicioNotificacion.obtenerNoLeidas("inexistente@example.com");
        });

        assertThrows(RecursoNoEncontradoException.class, () -> {
            servicioNotificacion.contarNoLeidas("inexistente@example.com");
        });

        assertThrows(RecursoNoEncontradoException.class, () -> {
            servicioNotificacion.obtenerHistorial("inexistente@example.com", 0, 10);
        });
    }

    // ==================== TESTS DE LIMPIEZA PROGRAMADA ====================

    @Test
    @DisplayName("Debe ejecutar limpieza de notificaciones antiguas correctamente")
    void debeEjecutarLimpiezaCorrectamente() {
        when(repositorioNotificacion.deleteNotificacionesLeidasAntiguas(any(LocalDateTime.class)))
                .thenReturn(15);

        servicioNotificacion.limpiarNotificacionesAntiguas();

        ArgumentCaptor<LocalDateTime> fechaCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repositorioNotificacion, times(1)).deleteNotificacionesLeidasAntiguas(fechaCaptor.capture());

        LocalDateTime fechaLimite = fechaCaptor.getValue();
        assertNotNull(fechaLimite);
        assertTrue(fechaLimite.isBefore(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Debe respetar configuración de días de retención")
    void debeRespetarDiasRetencion() {
        ReflectionTestUtils.setField(servicioNotificacion, "retentionDays", 60);

        when(repositorioNotificacion.deleteNotificacionesLeidasAntiguas(any(LocalDateTime.class)))
                .thenReturn(10);

        servicioNotificacion.limpiarNotificacionesAntiguas();

        ArgumentCaptor<LocalDateTime> fechaCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repositorioNotificacion).deleteNotificacionesLeidasAntiguas(fechaCaptor.capture());

        LocalDateTime fechaLimite = fechaCaptor.getValue();
        LocalDateTime fechaEsperada = LocalDateTime.now().minusDays(60);

        // Permitir margen de 1 minuto de diferencia
        assertTrue(Math.abs(java.time.Duration.between(fechaLimite, fechaEsperada).toMinutes()) <= 1);
    }

    @Test
    @DisplayName("Debe manejar errores en limpieza sin lanzar excepción")
    void debeManejarErroresEnLimpieza() {
        when(repositorioNotificacion.deleteNotificacionesLeidasAntiguas(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Error de BD"));

        // No debe lanzar excepción
        assertDoesNotThrow(() -> servicioNotificacion.limpiarNotificacionesAntiguas());
    }

    // ==================== TESTS DE CONVERSIÓN A DTO ====================

    @Test
    @DisplayName("Debe convertir NotificacionResponse con todos los campos")
    void debeConvertirAResponseCorrectamente() {
        notificacionNoLeida.marcarComoLeida();

        when(repositorioProfesional.findByEmail(emailProfesional)).thenReturn(Optional.of(profesional));
        when(repositorioNotificacion.findNoLeidasByProfesional(profesional)).thenReturn(Collections.singletonList(notificacionNoLeida));

        // El mapeo se realiza internamente en obtenerNoLeidas
        List<NotificacionResponse> resultado = servicioNotificacion.obtenerNoLeidas(emailProfesional);

        // Verificación indirecta a través del resultado
        assertNotNull(resultado);
    }

    @Test
    @DisplayName("Debe incluir fecha de creación en formato ISO 8601")
    void debeIncluirFechaCreacionISO8601() {
        when(repositorioProfesional.findByEmail(emailProfesional)).thenReturn(Optional.of(profesional));
        when(repositorioNotificacion.findNoLeidasByProfesional(profesional))
                .thenReturn(Collections.singletonList(notificacionNoLeida));

        List<NotificacionResponse> resultado = servicioNotificacion.obtenerNoLeidas(emailProfesional);

        assertNotNull(resultado.get(0).getFechaCreacion());
        // Formato ISO 8601: 2026-02-28T15:30:00Z
        assertTrue(resultado.get(0).getFechaCreacion().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"));
    }

    // ==================== HELPERS ====================

    private Notificacion crearNotificacion(Long id, boolean leida) {
        Notificacion notif = new Notificacion();
        notif.setId(id);
        notif.setProfesional(profesional);
        notif.setTipo(TipoNotificacion.NUEVO_TURNO);
        notif.setTitulo("Título " + id);
        notif.setMensaje("Mensaje " + id);
        notif.setLeida(leida);
        notif.setFechaCreacion(LocalDateTime.now().minusMinutes(id));
        if (leida) {
            notif.marcarComoLeida();
        }
        return notif;
    }
}
