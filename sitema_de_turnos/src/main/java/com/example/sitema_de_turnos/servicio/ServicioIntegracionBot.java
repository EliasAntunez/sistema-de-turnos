package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.bot.BotConfigResponseDto;
import com.example.sitema_de_turnos.dto.bot.BotCrearTurnoRequestDto;
import com.example.sitema_de_turnos.dto.bot.BotCrearTurnoResponseDto;
import com.example.sitema_de_turnos.dto.bot.BotServicioResponseDto;
import com.example.sitema_de_turnos.dto.publico.CrearTurnoRequest;
import com.example.sitema_de_turnos.dto.publico.TurnoResponsePublico;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.excepcion.ValidacionException;
import com.example.sitema_de_turnos.modelo.BotConfiguracion;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.EstadoTurno;
import com.example.sitema_de_turnos.modelo.PerfilProfesional;
import com.example.sitema_de_turnos.modelo.ProfesionalServicio;
import com.example.sitema_de_turnos.modelo.Servicio;
import com.example.sitema_de_turnos.repositorio.RepositorioBotConfiguracion;
import com.example.sitema_de_turnos.repositorio.RepositorioEmpresa;
import com.example.sitema_de_turnos.repositorio.RepositorioPerfilProfesional;
import com.example.sitema_de_turnos.repositorio.RepositorioProfesionalServicio;
import com.example.sitema_de_turnos.repositorio.RepositorioServicio;
import com.example.sitema_de_turnos.repositorio.RepositorioTurno;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioIntegracionBot {

    private static final List<EstadoTurno> ESTADOS_OCUPANTES_AGENDA =
        List.of(EstadoTurno.CONFIRMADO, EstadoTurno.PENDIENTE_PAGO);

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final RepositorioBotConfiguracion repositorioBotConfiguracion;
    private final RepositorioEmpresa repositorioEmpresa;
    private final RepositorioServicio repositorioServicio;
    private final RepositorioPerfilProfesional repositorioPerfilProfesional;
    private final RepositorioProfesionalServicio repositorioProfesionalServicio;
    private final RepositorioTurno repositorioTurno;
    private final ServicioTurno servicioTurno;

    @Transactional(readOnly = true)
    public BotConfigResponseDto obtenerConfig(String instanciaWhatsapp) {
        BotConfiguracion config = repositorioBotConfiguracion.findByInstanciaWhatsapp(instanciaWhatsapp)
            .orElseThrow(() -> new RecursoNoEncontradoException("Configuración de bot no encontrada para la instancia de WhatsApp"));

        return new BotConfigResponseDto(
            config.getTenantId(),
            config.getPromptPersonalizado(),
            Boolean.TRUE.equals(config.getEstadoBot())
        );
    }

    @Transactional(readOnly = true)
    public List<BotServicioResponseDto> obtenerServiciosPorTenant(Long tenantId) {
        Empresa empresa = repositorioEmpresa.findById(tenantId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Tenant no encontrado"));

        List<Servicio> servicios = repositorioServicio.findByEmpresaAndActivoTrue(empresa);

        return servicios.stream()
            .map(servicio -> new BotServicioResponseDto(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getPrecio(),
                servicio.getDuracionMinutos()
            ))
            .toList();
    }

    public BotCrearTurnoResponseDto crearTurno(Long tenantId, BotCrearTurnoRequestDto request) {
        Empresa empresa = repositorioEmpresa.findById(tenantId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Tenant no encontrado"));

        if (!Boolean.TRUE.equals(empresa.getActiva())) {
            throw new ValidacionException("El tenant está inactivo");
        }

        Servicio servicio = repositorioServicio.findById(request.getServicioId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado"));

        if (!servicio.getEmpresa().getId().equals(tenantId)) {
            throw new ValidacionException("El servicioId no pertenece al tenantId indicado");
        }

        if (!Boolean.TRUE.equals(servicio.getActivo())) {
            throw new ValidacionException("El servicio está inactivo");
        }

        LocalDateTime fechaHora = request.getFechaHora().withSecond(0).withNano(0);
        LocalDate fecha = fechaHora.toLocalDate();
        LocalTime horaInicio = fechaHora.toLocalTime();
        int buffer = servicio.getBufferMinutos() != null
            ? servicio.getBufferMinutos()
            : (empresa.getBufferPorDefecto() != null ? empresa.getBufferPorDefecto() : 10);
        LocalTime horaFin = horaInicio.plusMinutes(servicio.getDuracionMinutos()).plusMinutes(buffer);

        PerfilProfesional profesional = seleccionarProfesionalDisponible(empresa, servicio, fecha, horaInicio, horaFin);

        CrearTurnoRequest crearTurnoRequest = new CrearTurnoRequest();
        crearTurnoRequest.setServicioId(servicio.getId());
        crearTurnoRequest.setProfesionalId(profesional.getId());
        crearTurnoRequest.setFecha(fecha.toString());
        crearTurnoRequest.setHoraInicio(horaInicio.format(FORMATO_HORA));
        crearTurnoRequest.setNombreCliente(request.getClienteNombre().trim());
        crearTurnoRequest.setEmailCliente(generarEmailTecnicoCliente(tenantId, request.getClienteNombre()));
        crearTurnoRequest.setTelefonoCliente(null);
        crearTurnoRequest.setObservaciones("Reserva creada por integración conversacional");

        TurnoResponsePublico turnoCreado = servicioTurno.crearTurnoPublico(empresa.getSlug(), null, crearTurnoRequest);

        return new BotCrearTurnoResponseDto(
            turnoCreado.getId(),
            tenantId,
            turnoCreado.getServicioId(),
            turnoCreado.getProfesionalId(),
            turnoCreado.getEstado(),
            turnoCreado.getFecha(),
            turnoCreado.getHoraInicio(),
            turnoCreado.getHoraFin()
        );
    }

    private PerfilProfesional seleccionarProfesionalDisponible(
        Empresa empresa,
        Servicio servicio,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin
    ) {
        List<PerfilProfesional> profesionalesActivos = repositorioPerfilProfesional.findByEmpresaAndActivoTrue(empresa);

        if (profesionalesActivos.isEmpty()) {
            throw new RecursoNoEncontradoException("No hay profesionales activos para el tenant indicado");
        }

        return profesionalesActivos.stream()
            .filter(profesional -> tieneServicioHabilitado(profesional, servicio))
            .filter(profesional -> !repositorioTurno.existeSolapamiento(
                profesional,
                fecha,
                horaInicio,
                horaFin,
                ESTADOS_OCUPANTES_AGENDA
            ))
            .findFirst()
            .orElseThrow(() -> new ValidacionException("No hay profesionales disponibles para el servicio en la fecha y hora indicada"));
    }

    private boolean tieneServicioHabilitado(PerfilProfesional profesional, Servicio servicio) {
        ProfesionalServicio profesionalServicio = repositorioProfesionalServicio.findByProfesionalAndServicio(profesional, servicio)
            .orElse(null);
        return profesionalServicio != null && Boolean.TRUE.equals(profesionalServicio.getActivo());
    }

    private String generarEmailTecnicoCliente(Long tenantId, String clienteNombre) {
        String normalizado = Normalizer.normalize(clienteNombre, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", ".")
            .replaceAll("^\\.+|\\.+$", "");

        if (normalizado.isBlank()) {
            normalizado = "cliente";
        }

        String sufijo = Integer.toUnsignedString((clienteNombre + ":" + tenantId).hashCode(), 36);
        return "bot+" + normalizado + "." + sufijo + "@chatbot.local";
    }
}
