package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.*;
import com.example.sitema_de_turnos.dto.notificacion.CancelacionBloqueoData;
import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.excepcion.ValidacionException;
import com.example.sitema_de_turnos.modelo.*;
import com.example.sitema_de_turnos.evento.CancelacionBloqueoEvent;
import com.example.sitema_de_turnos.repositorio.RepositorioBloqueoFecha;
import com.example.sitema_de_turnos.repositorio.RepositorioTurno;
import com.example.sitema_de_turnos.repositorio.RepositorioPoliticaCancelacion;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Gestiona el ciclo de vida de los bloqueos de agenda de un profesional.
 * Verifica conflictos con turnos existentes, resuelve cancelaciones masivas
 * y publica eventos para notificar a los clientes afectados.
 */
@Service
@RequiredArgsConstructor
public class ServicioBloqueoFecha {

    private static final Logger log = LoggerFactory.getLogger(ServicioBloqueoFecha.class);

    /** Estados terminales que no generan conflicto de bloqueo. */
    private static final List<EstadoTurno> ESTADOS_TERMINALES =
            List.of(EstadoTurno.CANCELADO, EstadoTurno.ATENDIDO, EstadoTurno.NO_ASISTIO);
    private static final ZoneId ZONA_ARGENTINA = ZoneId.of("America/Argentina/Buenos_Aires");

    private final RepositorioBloqueoFecha repositorioBloqueoFecha;
    private final RepositorioTurno repositorioTurno;
    private final ServicioValidacionProfesional servicioValidacionProfesional;
    private final RepositorioPoliticaCancelacion repositorioPoliticaCancelacion;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public BloqueoFechaResponse crearBloqueo(String emailProfesional, BloqueoFechaRequest request) {
        // Validar fechas
        if (request.getFechaFin() != null && request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior o igual a la fecha de inicio");
        }

        // Obtener profesional
        PerfilProfesional profesional = obtenerProfesional(emailProfesional);

        // Verificar solapamientos con bloqueos existentes
        List<BloqueoFecha> bloqueosExistentes;
        if (request.getFechaFin() != null) {
            bloqueosExistentes = repositorioBloqueoFecha.findBloqueosEnRango(
                    profesional, request.getFechaInicio(), request.getFechaFin());
        } else {
            bloqueosExistentes = repositorioBloqueoFecha.findBloqueoEnFecha(
                    profesional, request.getFechaInicio());
        }

        if (!bloqueosExistentes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ya existe un bloqueo que se solapa con las fechas indicadas");
        }

        // NUEVA VALIDACIÓN: Verificar conflictos con turnos
        ConflictosBloqueoResponse conflictos = verificarConflictosConTurnos(profesional, request);
        if (conflictos.isTieneConflictos()) {
            throw new ValidacionException("Existen turnos en conflicto. Use el endpoint de resolución de conflictos.");
        }

        // Crear bloqueo
        BloqueoFecha bloqueo = new BloqueoFecha();
        bloqueo.setProfesional(profesional);
        bloqueo.setFechaInicio(request.getFechaInicio());
        bloqueo.setFechaFin(request.getFechaFin());
        bloqueo.setMotivo(request.getMotivo());
        bloqueo.setActivo(true);

        bloqueo = repositorioBloqueoFecha.save(bloqueo);

        return convertirAResponse(bloqueo);
    }

    /**
     * Verifica conflictos delegando la resolución del profesional al servicio de validación.
     * Punto de entrada para controladores: evita que el controlador manipule entidades directamente.
     */
    @Transactional(readOnly = true)
    public ConflictosBloqueoResponse verificarConflictosConTurnos(String emailProfesional, BloqueoFechaRequest request) {
        PerfilProfesional profesional = obtenerProfesional(emailProfesional);
        return verificarConflictosConTurnos(profesional, request);
    }

    /**
     * Verificar si existen turnos en conflicto con el bloqueo.
     *
     * OPTIMIZADO: usa JOIN FETCH para evitar N+1 queries.
     * PLUS: detecta violaciones a la política de cancelación de la empresa
     * y enriquece cada ConflictoTurnoDTO con el flag violaPolitica (solo informativo).
     */
    @Transactional(readOnly = true)
    public ConflictosBloqueoResponse verificarConflictosConTurnos(PerfilProfesional profesional, BloqueoFechaRequest request) {
        LocalDate fechaInicio = request.getFechaInicio();
        LocalDate fechaFin = request.getFechaFin() != null ? request.getFechaFin() : request.getFechaInicio();
        ZonedDateTime ahoraArgentina = ZonedDateTime.now(ZONA_ARGENTINA);
        LocalDate fechaActual = ahoraArgentina.toLocalDate();
        LocalTime horaActual = ahoraArgentina.toLocalTime();

        // 1. Buscar turnos conflictivos con JOIN FETCH (1 query en lugar de 1+3N)
        List<Turno> turnosConflictivos = repositorioTurno.findConflictosBloqueo(
            profesional, fechaInicio, fechaFin, fechaActual, horaActual, ESTADOS_TERMINALES);

        ConflictosBloqueoResponse response = new ConflictosBloqueoResponse();
        response.setTieneConflictos(!turnosConflictivos.isEmpty());
        response.setCantidadConflictos(turnosConflictivos.size());

        if (!turnosConflictivos.isEmpty()) {
            // 2. Obtener política de cancelación activa de la empresa (si existe)
            Optional<PoliticaCancelacion> politica = repositorioPoliticaCancelacion
                    .findByEmpresaAndActivaTrue(profesional.getEmpresa())
                    .stream()
                    .filter(p -> p.getTipo() == TipoPoliticaCancelacion.CANCELACION
                              || p.getTipo() == TipoPoliticaCancelacion.AMBOS)
                    .findFirst();

            LocalDateTime ahora = ahoraArgentina.toLocalDateTime();

            // 3. Mapear a DTO enriquecido con flag de política
            List<ConflictoTurnoDTO> conflictos = turnosConflictivos.stream()
                    .map(turno -> {
                        ConflictoTurnoDTO dto = convertirAConflictoDTO(turno);
                        politica.ifPresent(p -> {
                            LocalDateTime inicioTurno = LocalDateTime.of(turno.getFecha(), turno.getHoraInicio());
                            long horasHastaTurno = ChronoUnit.HOURS.between(ahora, inicioTurno);
                            if (horasHastaTurno >= 0 && horasHastaTurno < p.getHorasLimiteCancelacion()) {
                                dto.setViolaPolitica(true);
                                dto.setDescripcionViolacion("Faltan " + horasHastaTurno + "h para el turno. "
                                        + "La política requiere " + p.getHorasLimiteCancelacion()
                                        + "h de anticipación para cancelar.");
                            }
                        });
                        return dto;
                    })
                    .collect(Collectors.toList());

            boolean hayViolacion = conflictos.stream().anyMatch(ConflictoTurnoDTO::isViolaPolitica);
            response.setTurnosConflictivos(conflictos);
            response.setHayViolacionPolitica(hayViolacion);
            response.setMensaje("Se encontraron " + turnosConflictivos.size()
                    + " turno(s) que deben ser resueltos antes de crear el bloqueo.");
        } else {
            response.setHayViolacionPolitica(false);
            response.setMensaje("No hay conflictos con turnos existentes.");
        }

        return response;
    }

    /**
     * Crear bloqueo con resolución de conflictos
     */
    @Transactional
    public BloqueoFechaResponse crearBloqueoConResolucion(String emailProfesional, ResolucionConflictoRequest request) {
        PerfilProfesional profesional = obtenerProfesional(emailProfesional);

        // Verificar conflictos nuevamente
        ConflictosBloqueoResponse conflictos = verificarConflictosConTurnos(profesional, request.getBloqueo());

        if (!conflictos.isTieneConflictos()) {
            // No hay conflictos, crear normalmente
            return crearBloqueoSinValidacionConflictos(profesional, request.getBloqueo());
        }

        // Resolver según la acción elegida
        if (request.getAccion() == ResolucionConflictoRequest.AccionResolucion.CANCELAR_EN_RANGO) {

            // 1. Cargar datos para notificación ANTES del bulk UPDATE,
            //    mientras las relaciones (cliente, servicio, empresa) están en el contexto JPA.
            List<Long> ids = conflictos.getTurnosConflictivos().stream()
                    .map(ConflictoTurnoDTO::getTurnoId)
                    .collect(Collectors.toList());

            List<CancelacionBloqueoData> datosNotificacion = repositorioTurno
                    .findByIdsParaNotificacion(ids)
                    .stream()
                    .map(this::construirDatoNotificacion)
                    .collect(Collectors.toList());

            // 2. Cancelar masivamente (1 sola query UPDATE)
            cancelarEnRango(conflictos.getTurnosConflictivos());

            // 3. Crear el bloqueo
            BloqueoFechaResponse resultado = crearBloqueoSinValidacionConflictos(profesional, request.getBloqueo());

            // 4. Publicar evento: el listener @TransactionalEventListener(AFTER_COMMIT)
            //    garantiza que los emails se envían solo si la transacción confirmó en DB.
            eventPublisher.publishEvent(new CancelacionBloqueoEvent(datosNotificacion));

            return resultado;

        } else {
            throw new ValidacionException("Acción no válida");
        }
    }

    private BloqueoFechaResponse crearBloqueoSinValidacionConflictos(PerfilProfesional profesional, BloqueoFechaRequest request) {
        BloqueoFecha bloqueo = new BloqueoFecha();
        bloqueo.setProfesional(profesional);
        bloqueo.setFechaInicio(request.getFechaInicio());
        bloqueo.setFechaFin(request.getFechaFin());
        bloqueo.setMotivo(request.getMotivo());
        bloqueo.setActivo(true);
        bloqueo = repositorioBloqueoFecha.save(bloqueo);
        return convertirAResponse(bloqueo);
    }

    private CancelacionBloqueoData construirDatoNotificacion(Turno turno) {
        PerfilProfesional p = turno.getProfesional();
        return CancelacionBloqueoData.builder()
                .clienteEmail(turno.getCliente().getEmail())
                .clienteNombre(turno.getCliente().getNombre())
                .fecha(turno.getFecha())
                .horaInicio(turno.getHoraInicio())
                .servicioNombre(turno.getServicio().getNombre())
                .profesionalNombre(p.getUsuario().getNombre() + " " + p.getUsuario().getApellido())
                .empresaNombre(p.getEmpresa().getNombre())
                .empresaSlug(p.getEmpresa().getSlug())
                .build();
    }

    private void cancelarEnRango(List<ConflictoTurnoDTO> conflictos) {
        if (conflictos.isEmpty()) return;
        ZonedDateTime ahoraArgentina = ZonedDateTime.now(ZONA_ARGENTINA);
        LocalDateTime ahoraLocal = ahoraArgentina.toLocalDateTime();
        LocalDate fechaActual = ahoraArgentina.toLocalDate();
        LocalTime horaActual = ahoraArgentina.toLocalTime();
        List<Long> ids = conflictos.stream()
                .map(ConflictoTurnoDTO::getTurnoId)
                .collect(Collectors.toList());
        int actualizados = repositorioTurno.cancelarTurnosMasivamente(
                ids,
                EstadoTurno.CANCELADO,
                "Cancelado por creación de bloqueo de fecha",
                "PROFESIONAL",
            ahoraLocal,
            fechaActual,
            horaActual
        );
        if (actualizados != ids.size()) {
            log.warn("cancelarEnRango: se esperaba cancelar {} turno(s) pero se actualizaron {}. "
                    + "Algunos pueden haber alcanzado un estado terminal antes del bloqueo.",
                    ids.size(), actualizados);
        }
    }

    private ConflictoTurnoDTO convertirAConflictoDTO(Turno turno) {
        ConflictoTurnoDTO dto = new ConflictoTurnoDTO();
        dto.setTurnoId(turno.getId());
        dto.setFecha(turno.getFecha());
        dto.setHoraInicio(turno.getHoraInicio());
        dto.setHoraFin(turno.getHoraFin());
        dto.setClienteNombre(turno.getCliente().getNombre());
        dto.setClienteTelefono(turno.getCliente().getTelefono());
        dto.setServicioNombre(turno.getServicio().getNombre());
        dto.setEstado(turno.getEstado());
        return dto;
    }

    @Transactional
    public BloqueoFechaResponse actualizarBloqueo(String emailProfesional, Long bloqueoId, 
                                                   BloqueoFechaRequest request) {
        // Validar fechas
        if (request.getFechaFin() != null && request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior o igual a la fecha de inicio");
        }

        // Obtener profesional
        PerfilProfesional profesional = obtenerProfesional(emailProfesional);

        // Obtener bloqueo y validar pertenencia
        BloqueoFecha bloqueo = repositorioBloqueoFecha.findById(bloqueoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Bloqueo no encontrado"));

        if (!bloqueo.getProfesional().getId().equals(profesional.getId())) {
            throw new AccesoDenegadoException("No puede modificar el bloqueo de otro profesional");
        }

        // Verificar solapamientos (excluyendo el bloqueo actual)
        List<BloqueoFecha> bloqueosExistentes;
        if (request.getFechaFin() != null) {
            bloqueosExistentes = repositorioBloqueoFecha.findBloqueosEnRango(
                    profesional, request.getFechaInicio(), request.getFechaFin());
        } else {
            bloqueosExistentes = repositorioBloqueoFecha.findBloqueoEnFecha(
                    profesional, request.getFechaInicio());
        }

        // Filtrar el bloqueo actual
        bloqueosExistentes = bloqueosExistentes.stream()
                .filter(b -> !b.getId().equals(bloqueoId))
                .collect(Collectors.toList());

        if (!bloqueosExistentes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ya existe un bloqueo que se solapa con las fechas indicadas");
        }

        // Actualizar bloqueo
        bloqueo.setFechaInicio(request.getFechaInicio());
        bloqueo.setFechaFin(request.getFechaFin());
        bloqueo.setMotivo(request.getMotivo());

        bloqueo = repositorioBloqueoFecha.save(bloqueo);

        return convertirAResponse(bloqueo);
    }

    @Transactional(readOnly = true)
    public List<BloqueoFechaResponse> obtenerBloqueosPropios(String emailProfesional) {
        PerfilProfesional profesional = obtenerProfesional(emailProfesional);

        List<BloqueoFecha> bloqueos = 
                repositorioBloqueoFecha.findByProfesionalAndActivoTrueOrderByFechaInicioAsc(profesional);

        return bloqueos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarBloqueo(String emailProfesional, Long bloqueoId) {
        // Obtener profesional
        PerfilProfesional profesional = obtenerProfesional(emailProfesional);

        // Obtener bloqueo y validar pertenencia
        BloqueoFecha bloqueo = repositorioBloqueoFecha.findById(bloqueoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Bloqueo no encontrado"));

        if (!bloqueo.getProfesional().getId().equals(profesional.getId())) {
            throw new AccesoDenegadoException("No puede eliminar el bloqueo de otro profesional");
        }

        // Soft delete
        bloqueo.setActivo(false);
        repositorioBloqueoFecha.save(bloqueo);
    }

    /**
     * CORREGIDO: Ahora valida profesional activo + empresa activa
     */
    private PerfilProfesional obtenerProfesional(String email) {
        return servicioValidacionProfesional.validarYObtenerProfesional(email);
    }

    private BloqueoFechaResponse convertirAResponse(BloqueoFecha bloqueo) {
        BloqueoFechaResponse response = new BloqueoFechaResponse();
        response.setId(bloqueo.getId());
        response.setProfesionalId(bloqueo.getProfesional().getId());
        response.setFechaInicio(bloqueo.getFechaInicio());
        response.setFechaFin(bloqueo.getFechaFin());
        response.setMotivo(bloqueo.getMotivo());
        response.setActivo(bloqueo.getActivo());
        response.setFechaCreacion(bloqueo.getFechaCreacion());
        return response;
    }
}
