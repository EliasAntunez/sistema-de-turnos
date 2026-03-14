package com.example.sitema_de_turnos.servicio;
import com.example.sitema_de_turnos.util.NormalizadorDatos;

import com.example.sitema_de_turnos.dto.publico.CrearTurnoRequest;
import com.example.sitema_de_turnos.dto.publico.TurnoResponsePublico;
import com.example.sitema_de_turnos.dto.publico.ReservaModificarRequest;
import com.example.sitema_de_turnos.dto.publico.TurnoResponseProfesional;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.excepcion.SolapamientoException;
import com.example.sitema_de_turnos.excepcion.ValidacionException;
import com.example.sitema_de_turnos.modelo.*;
import com.example.sitema_de_turnos.repositorio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServicioTurno {

    private final RepositorioTurno repositorioTurno;
    private final RepositorioCliente repositorioCliente;
    private final RepositorioServicio repositorioServicio;
    private final RepositorioPerfilProfesional repositorioPerfilProfesional;
    private final RepositorioEmpresa repositorioEmpresa;
    private final RepositorioBloqueoFecha repositorioBloqueoFecha;
    private final RepositorioDisponibilidadProfesional repositorioDisponibilidadProfesional;
    private final RepositorioHorarioEmpresa repositorioHorarioEmpresa;
    private final RepositorioPago repositorioPago;
    private final ServicioNotificacion servicioNotificacion;

        /** Estados terminales que no deben bloquear agenda ni participar en el índice parcial. */
        private static final List<EstadoTurno> ESTADOS_TERMINALES =
            List.of(EstadoTurno.CANCELADO, EstadoTurno.ATENDIDO, EstadoTurno.NO_ASISTIO);

    // ✅ M2: Constantes estáticas para formateo de fechas
    private static final DateTimeFormatter FORMATTER_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMATTER_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATTER_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Obtener fecha/hora actual en la zona horaria de la empresa.
     * CRÍTICO: Todas las validaciones temporales ("turno pasado", "hoy", etc.)
     * deben usar este método en lugar de LocalDate.now() / LocalDateTime.now()
     * para soporte multi-timezone.
     */
    private ZonedDateTime obtenerAhoraEmpresa(Empresa empresa) {
        ZoneId zoneId = ZoneId.of(empresa.getTimezone());
        return ZonedDateTime.now(zoneId);
    }

    /**
     * Crear un turno desde la vista pública.
     * ISOLATION.REPEATABLE_READ: previene dirty reads y non-repeatable reads.
     * La barrera atómica real contra race conditions es el índice único parcial
     * idx_turno_no_overlap (PostgreSQL). Si dos threads pasan el check en memoria
     * simultáneamente, el segundo INSERT viola el índice → DataIntegrityViolationException
     * → capturada y relanzada como SolapamientoException → HTTP 409.
     * SERIALIZABLE no es necesario y genera falsos positivos de "serialization failure".
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TurnoResponsePublico crearTurnoPublico(String empresaSlug, Cliente clienteAutenticado, CrearTurnoRequest request) {
        // 1. Obtener empresa
        Empresa empresa = repositorioEmpresa.findBySlugAndActivaTrue(empresaSlug)
            .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));

        // 2. Obtener servicio
        Servicio servicio = repositorioServicio.findById(request.getServicioId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado"));

        if (!servicio.getActivo() || !servicio.getEmpresa().getId().equals(empresa.getId())) {
            throw new ValidacionException("Servicio no disponible");
        }

        // 3. Obtener profesional
        PerfilProfesional profesional = repositorioPerfilProfesional.findById(request.getProfesionalId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        if (!profesional.getActivo() || !profesional.getEmpresa().getId().equals(empresa.getId())) {
            throw new ValidacionException("Profesional no disponible");
        }

        // 4. Parsear fecha y hora
        LocalDate fecha = LocalDate.parse(request.getFecha(), DateTimeFormatter.ISO_LOCAL_DATE);
        LocalTime horaInicio = LocalTime.parse(request.getHoraInicio(), DateTimeFormatter.ofPattern("HH:mm"));
        
        // Calcular horaFin incluyendo buffer (duración + buffer del servicio o buffer por defecto de la empresa)
        int buffer = servicio.getBufferMinutos() != null ? servicio.getBufferMinutos() : empresa.getBufferPorDefecto();
        LocalTime horaFin = horaInicio.plusMinutes(servicio.getDuracionMinutos()).plusMinutes(buffer);

        // 4.1. Validar que el profesional NO tenga bloqueo en esta fecha
        if (!repositorioBloqueoFecha.findBloqueoEnFecha(profesional, fecha).isEmpty()) {
            throw new ValidacionException("El profesional no está disponible en la fecha seleccionada");
        }

        // 5. Validar que la fecha no sea pasada (usando timezone de empresa)
        ZonedDateTime ahoraEmpresa = obtenerAhoraEmpresa(empresa);
        LocalDate fechaHoyEmpresa = ahoraEmpresa.toLocalDate();
        LocalTime horaActualEmpresa = ahoraEmpresa.toLocalTime();
        
        if (fecha.isBefore(fechaHoyEmpresa)) {
            throw new ValidacionException("No se puede reservar en una fecha pasada");
        }
        
        // 5.1. Validar tiempo mínimo de anticipación para el día actual
        if (fecha.isEqual(fechaHoyEmpresa)) {
            int tiempoMinimo = empresa.getTiempoMinimoAnticipacionMinutos() != null ? 
                empresa.getTiempoMinimoAnticipacionMinutos() : 30;
            LocalTime horaLimite = horaActualEmpresa.plusMinutes(tiempoMinimo);
            if (horaInicio.isBefore(horaLimite)) {
                throw new ValidacionException("Debe reservar con al menos " + tiempoMinimo + 
                    " minutos de anticipación. Próximo horario disponible: " + horaLimite.format(FORMATTER_HORA));
            }
        }

        // 6. Validar límite de días de reserva
        int diasMaximos = empresa.getDiasMaximosReserva() != null ? empresa.getDiasMaximosReserva() : 30;
        if (fecha.isAfter(fechaHoyEmpresa.plusDays(diasMaximos))) {
            throw new ValidacionException("No se puede reservar con más de " + diasMaximos + " días de anticipación");
        }

        // 7. Verificar disponibilidad (sin solapamiento)
        if (repositorioTurno.existeSolapamiento(profesional, fecha, horaInicio, horaFin, ESTADOS_TERMINALES)) {
            throw new SolapamientoException("El horario seleccionado ya no está disponible");
        }

        // 8. Obtener o crear cliente
        Cliente cliente;
        if (clienteAutenticado != null) {
            // Verificar que el cliente autenticado pertenezca a la empresa
            if (!clienteAutenticado.getEmpresa().getId().equals(empresa.getId())) {
                throw new AccesoDenegadoException("No tienes permisos para reservar en esta empresa");
            }
            cliente = clienteAutenticado;
        } else {
            cliente = obtenerOCrearCliente(empresa, request);
        }

        // 9. Crear turno
        Turno turno = new Turno();
        turno.setEmpresa(empresa);
        turno.setServicio(servicio);
        turno.setProfesional(profesional);
        turno.setCliente(cliente);
        turno.setFecha(fecha);
        turno.setHoraInicio(horaInicio);
        turno.setHoraFin(horaFin);
        turno.setDuracionMinutos(servicio.getDuracionMinutos()); // Solo duración del servicio
        turno.setBufferMinutos(buffer); // Buffer aplicado en este turno
        turno.setPrecio(servicio.getPrecio());
        boolean requiereSena = Boolean.TRUE.equals(servicio.getRequiereSena());
        turno.setEstado(requiereSena ? EstadoTurno.PENDIENTE_PAGO : EstadoTurno.CONFIRMADO);
        turno.setObservaciones(request.getObservaciones());

        try {
            turno = repositorioTurno.save(turno);
        } catch (DataIntegrityViolationException e) {
            throw new SolapamientoException("El turno ya fue tomado por otro cliente. Por favor, selecciona otro horario.", e);
        }

        if (requiereSena) {
            if (servicio.getMontoSena() == null || servicio.getMontoSena().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new ValidacionException("El servicio requiere seña, pero no tiene un monto de seña válido configurado");
            }

            Pago pago = new Pago();
            pago.setTurno(turno);
            pago.setMonto(servicio.getMontoSena());
            pago.setMetodoPago(MetodoPago.MERCADO_PAGO);
            pago.setEstado(EstadoPago.PENDIENTE);
            pago.setReferenciaExterna(null);
            repositorioPago.save(pago);
        }

        // 10. Enviar notificación al profesional
        enviarNotificacionNuevoTurno(turno);

        // 11. Mapear respuesta PÚBLICA (sin exponer datos de otros clientes)
        return mapearATurnoResponsePublico(turno);
    }

    /**
     * Modificar datos permitidos de una reserva por el cliente propietario.
     */
    @Transactional
    public TurnoResponsePublico modificarReservaPorCliente(Long turnoId, Cliente cliente, ReservaModificarRequest request) {
        Turno turno = repositorioTurno.findById(turnoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        log.info("modificarReservaPorCliente called: turnoId={}, turnoClienteId={}, requesterClienteId={}", turnoId, turno.getCliente().getId(), cliente.getId());

        if (!turno.getCliente().getId().equals(cliente.getId())) {
            throw new AccesoDenegadoException("No tienes permiso para modificar este turno");
        }

        if (turno.getEstado() == EstadoTurno.CANCELADO || turno.getEstado() == EstadoTurno.ATENDIDO) {
            throw new ValidacionException("No se puede modificar un turno cancelado o ya atendido");
        }

        // Validar que el turno no sea pasado ni esté muy cerca (menos de 1 hora)
        ZonedDateTime ahoraEmpresa = obtenerAhoraEmpresa(turno.getEmpresa());
        LocalDateTime inicioTurno = LocalDateTime.of(turno.getFecha(), turno.getHoraInicio());
        LocalDateTime ahoraLocal = ahoraEmpresa.toLocalDateTime();

        if (inicioTurno.isBefore(ahoraLocal)) {
            log.info("Rechazando modificación: turno pasado turnoId={}, inicio={}, ahora={}", turnoId, inicioTurno, ahoraLocal);
            throw new ValidacionException("No se puede modificar un turno que ya pasó");
        }

        if (inicioTurno.minusHours(1).isBefore(ahoraLocal)) {
            log.info("Rechazando modificación: poca anticipación turnoId={}, inicio={}, ahora={}", turnoId, inicioTurno, ahoraLocal);
            throw new ValidacionException("No se puede modificar un turno con menos de 1 hora de anticipación");
        }

        boolean clienteActualizado = false;
        if (request.getNombreCliente() != null && !request.getNombreCliente().trim().isEmpty()) {
            cliente.setNombre(NormalizadorDatos.normalizarNombre(request.getNombreCliente()));
            clienteActualizado = true;
        }

        if (request.getObservaciones() != null) {
            turno.setObservaciones(request.getObservaciones());
        }

        if (clienteActualizado) {
            repositorioCliente.save(cliente);
        }

        turno = repositorioTurno.save(turno);
        return mapearATurnoResponsePublico(turno);
    }

    /**
     * Reprogramar (cambiar fecha/hora y opcionalmente profesional) una reserva por el cliente propietario.
     * ISOLATION.REPEATABLE_READ: misma estrategia que crearTurnoPublico.
     * La barrera atómica es el índice único parcial idx_turno_no_overlap en PostgreSQL.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TurnoResponsePublico reprogramarReservaPorCliente(Long turnoId, Cliente cliente, com.example.sitema_de_turnos.dto.publico.ReservaReprogramarRequest request) {
        Turno turno = repositorioTurno.findById(turnoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        log.info("reprogramarReservaPorCliente called: turnoId={}, turnoClienteId={}, requesterClienteId={}", turnoId, turno.getCliente().getId(), cliente.getId());

        if (!turno.getCliente().getId().equals(cliente.getId())) {
            throw new AccesoDenegadoException("No tienes permiso para reprogramar este turno");
        }

        if (turno.getEstado() == EstadoTurno.CANCELADO || turno.getEstado() == EstadoTurno.ATENDIDO) {
            throw new ValidacionException("No se puede reprogramar un turno cancelado o ya atendido");
        }

        // Validar que el turno origen no sea pasado ni esté muy cerca (menos de 1 hora)
        ZonedDateTime ahoraEmpresa = obtenerAhoraEmpresa(turno.getEmpresa());
        LocalDateTime inicioTurno = LocalDateTime.of(turno.getFecha(), turno.getHoraInicio());
        LocalDateTime ahoraLocal = ahoraEmpresa.toLocalDateTime();
        LocalDate fechaHoyEmpresa = ahoraEmpresa.toLocalDate();

        if (inicioTurno.isBefore(ahoraLocal)) {
            throw new ValidacionException("No se puede reprogramar un turno que ya pasó");
        }

        if (inicioTurno.minusHours(1).isBefore(ahoraLocal)) {
            throw new ValidacionException("No se puede reprogramar un turno con menos de 1 hora de anticipación");
        }

        // Campos solicitados (parse desde strings para compatibilidad de serialización en tests)
        java.time.LocalDate nuevaFecha = request.getFechaAsLocalDate();
        java.time.LocalTime nuevaHoraInicio = request.getHoraInicioAsLocalTime();
        Long nuevoProfesionalId = request.getProfesionalId();

        if (nuevaFecha == null || nuevaHoraInicio == null) {
            throw new ValidacionException("Fecha y hora inicio son requeridos para reprogramar");
        }

        // Validar fecha no pasada (usando timezone de empresa)
        if (nuevaFecha.isBefore(fechaHoyEmpresa)) {
            throw new ValidacionException("No se puede reprogramar a una fecha pasada");
        }

        // Validar tiempo mínimo de anticipación para la nueva fecha/hora
        LocalDateTime nuevoInicioTurno = LocalDateTime.of(nuevaFecha, nuevaHoraInicio);
        Integer tiempoMinimoAnticipacion = turno.getEmpresa().getTiempoMinimoAnticipacionMinutos();
        if (tiempoMinimoAnticipacion == null) {
            tiempoMinimoAnticipacion = 30; // Default
        }
        LocalDateTime horaLimiteReserva = ahoraLocal.plusMinutes(tiempoMinimoAnticipacion);
        
        if (nuevoInicioTurno.isBefore(horaLimiteReserva)) {
            throw new ValidacionException("La nueva fecha/hora debe ser al menos " + tiempoMinimoAnticipacion + " minutos después de ahora");
        }

        // Determinar profesional objetivo
        PerfilProfesional profesionalDestino;
        if (nuevoProfesionalId != null) {
            profesionalDestino = repositorioPerfilProfesional.findById(nuevoProfesionalId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));
            if (!profesionalDestino.getActivo() || !profesionalDestino.getEmpresa().getId().equals(turno.getEmpresa().getId())) {
                throw new ValidacionException("Profesional no disponible");
            }
        } else {
            profesionalDestino = turno.getProfesional();
        }

        // Validar bloqueos de fecha del profesional destino
        if (!repositorioBloqueoFecha.findBloqueoEnFecha(profesionalDestino, nuevaFecha).isEmpty()) {
            throw new ValidacionException("El profesional no está disponible en la fecha seleccionada");
        }

        // Calcular hora fin usando la duración y buffer del turno actual
        LocalTime nuevaHoraFin = nuevaHoraInicio.plusMinutes(turno.getDuracionMinutos()).plusMinutes(turno.getBufferMinutos());

        // Validar que el nuevo horario esté dentro de la disponibilidad configurada del profesional
        validarHorarioEnDisponibilidad(profesionalDestino, nuevaFecha, nuevaHoraInicio, nuevaHoraFin, turno.getEmpresa());

        // Validar solapamiento atómicamente (excluyendo el propio turno en su slot original)
        if (repositorioTurno.existeSolapamientoExcluyendo(profesionalDestino, nuevaFecha, nuevaHoraInicio, nuevaHoraFin, turno.getId(), ESTADOS_TERMINALES)) {
            throw new SolapamientoException("El horario seleccionado ya no está disponible");
        }

        // Si todo OK, aplicar cambios
        turno.setProfesional(profesionalDestino);
        turno.setFecha(nuevaFecha);
        turno.setHoraInicio(nuevaHoraInicio);
        turno.setHoraFin(nuevaHoraFin);

        try {
            turno = repositorioTurno.save(turno);
        } catch (DataIntegrityViolationException e) {
            throw new SolapamientoException("El turno ya fue tomado por otro cliente. Por favor, elija otro horario.", e);
        }

        log.info("Turno reprogramado: turnoId={}, profesional={}, nuevaFecha={}, nuevaHoraInicio={}, nuevaHoraFin={}",
            turno.getId(), turno.getProfesional().getId(), turno.getFecha(), turno.getHoraInicio(), turno.getHoraFin());

        // Enviar notificación de reprogramación al profesional
        enviarNotificacionReprogramacion(turno, inicioTurno, nuevoInicioTurno);

        return mapearATurnoResponsePublico(turno);
    }

    /**
     * Cancelar turno actuando como cliente autenticado.
     */
    @Transactional
    public void cancelarTurnoPorCliente(Long turnoId, Cliente cliente, String motivo) {
        Turno turno = repositorioTurno.findById(turnoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        if (!turno.getCliente().getId().equals(cliente.getId())) {
            throw new AccesoDenegadoException("No tienes permiso para cancelar este turno");
        }

        // Delega en la lógica existente que valida tiempos y estados
        cancelarTurno(turnoId, motivo, "CLIENTE");
    }

    /**
     * Obtener cliente existente o crear uno nuevo
     */
    private Cliente obtenerOCrearCliente(Empresa empresa, CrearTurnoRequest request) {
        // Normalizar email
        String emailNormalizado = NormalizadorDatos.normalizarEmail(request.getEmailCliente());
        
        // Buscar por email
        Optional<Cliente> clienteExistente = repositorioCliente.findByEmpresaAndEmailAndActivoTrue(
            empresa, 
            emailNormalizado
        );

        if (clienteExistente.isPresent()) {
            Cliente c = clienteExistente.get();
            // Si el cliente ya tiene cuenta registrada, NO se puede asociar automáticamente
            if (Boolean.TRUE.equals(c.getTieneUsuario())) {
                throw new com.example.sitema_de_turnos.excepcion.ConflictoException(
                    "El email pertenece a una cuenta registrada. Por favor, iniciá sesión o usá otro email."
                );
            }
            return c; // cliente invitado -> asociar
        }

        // Crear nuevo cliente invitado
        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setEmpresa(empresa);
        nuevoCliente.setNombre(NormalizadorDatos.normalizarNombre(request.getNombreCliente()));
        nuevoCliente.setEmail(emailNormalizado);
        
        // Solo setear teléfono si no está vacío
        String telefono = request.getTelefonoCliente();
        if (telefono != null && !telefono.trim().isEmpty()) {
            // Validar formato del teléfono
            if (!telefono.matches("^[+]?[0-9\\s\\-()]{8,20}$")) {
                throw new ValidacionException("Formato de teléfono inválido");
            }
            nuevoCliente.setTelefono(telefono);
        }
        
        nuevoCliente.setTieneUsuario(false);
        nuevoCliente.setTelefonoValidado(false);
        nuevoCliente.setActivo(true);

        try {
            return repositorioCliente.save(nuevoCliente);
        } catch (DataIntegrityViolationException e) {
            // Otro thread creó el cliente simultáneamente, reintentamos la búsqueda
            return repositorioCliente.findByEmpresaAndEmailAndActivoTrue(empresa, emailNormalizado)
                    .orElseThrow(() -> new ValidacionException("Error al crear el cliente. Intente nuevamente."));
        }
    }

    /**
     * Cancelar turno con validación de anticipación mínima (1 hora).
     * Privado: el acceso externo siempre pasa por cancelarTurnoPorCliente (que valida ownership).
     */
    @Transactional
    private void cancelarTurno(Long turnoId, String motivo, String canceladoPor) {
        Turno turno = repositorioTurno.findById(turnoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        if (turno.getEstado() == EstadoTurno.CANCELADO) {
            throw new ValidacionException("El turno ya está cancelado");
        }

        if (turno.getEstado() == EstadoTurno.ATENDIDO) {
            throw new ValidacionException("No se puede cancelar un turno ya atendido");
        }

        // Validar que el turno no sea pasado y que la cancelación respete la anticipación mínima (1 hora)
        ZonedDateTime ahoraEmpresa = obtenerAhoraEmpresa(turno.getEmpresa());
        LocalDateTime inicioTurno = LocalDateTime.of(turno.getFecha(), turno.getHoraInicio());
        LocalDateTime ahoraLocal = ahoraEmpresa.toLocalDateTime();

        if (inicioTurno.isBefore(ahoraLocal)) {
            throw new ValidacionException("No se puede cancelar un turno que ya pasó");
        }

        if (inicioTurno.minusHours(1).isBefore(ahoraLocal)) {
            throw new ValidacionException(
                "No se puede cancelar con menos de 1 hora de anticipación. " +
                "Por favor, comuníquese con la empresa al teléfono: " + 
                turno.getEmpresa().getTelefono()
            );
        }

        turno.setEstado(EstadoTurno.CANCELADO);
        turno.setMotivoCancelacion(motivo);
        turno.setCanceladoPor(canceladoPor);
        turno.setFechaCancelacion(ahoraLocal);

        repositorioTurno.save(turno);

        // Enviar notificación de cancelación al profesional
        enviarNotificacionCancelacion(turno, canceladoPor);
    }

    /**
     * Mapear Turno a TurnoResponsePublico (contexto público, sin datos sensibles)
     */
    private TurnoResponsePublico mapearATurnoResponsePublico(Turno turno) {
        TurnoResponsePublico response = new TurnoResponsePublico();
        response.setId(turno.getId());
        response.setServicioId(turno.getServicio().getId());
        response.setServicioNombre(turno.getServicio().getNombre());
        response.setProfesionalId(turno.getProfesional().getId());
        response.setProfesionalNombre(turno.getProfesional().getUsuario().getNombre());
        response.setProfesionalApellido(turno.getProfesional().getUsuario().getApellido());
        response.setFecha(turno.getFecha().toString());
        response.setHoraInicio(turno.getHoraInicio().format(FORMATTER_HORA));
        response.setHoraFin(turno.getHoraFin().format(FORMATTER_HORA));
        response.setHoraFinServicio(turno.getHoraInicio().plusMinutes(turno.getDuracionMinutos()).format(FORMATTER_HORA));
        response.setDuracionMinutos(turno.getDuracionMinutos());
        response.setPrecio(turno.getPrecio());
        response.setEstado(turno.getEstado().name());
        response.setObservaciones(turno.getObservaciones());
        response.setEmpresaTelefono(turno.getEmpresa().getTelefono());
        response.setEmpresaDatosBancarios(turno.getEmpresa().getDatosBancarios());
        return response;
    }

    /**
     * Mapear Turno a TurnoResponseProfesional (contexto profesional, con todos los datos)
     */
    private TurnoResponseProfesional mapearATurnoResponseProfesional(Turno turno) {
        TurnoResponseProfesional response = new TurnoResponseProfesional();
        response.setId(turno.getId());
        response.setServicioId(turno.getServicio().getId());
        response.setServicioNombre(turno.getServicio().getNombre());
        response.setProfesionalId(turno.getProfesional().getId());
        response.setProfesionalNombre(turno.getProfesional().getUsuario().getNombre());
        response.setProfesionalApellido(turno.getProfesional().getUsuario().getApellido());
        response.setFecha(turno.getFecha().toString());
        response.setHoraInicio(turno.getHoraInicio().format(FORMATTER_HORA));
        response.setHoraFin(turno.getHoraFin().format(FORMATTER_HORA));
        response.setDuracionMinutos(turno.getDuracionMinutos());
        response.setBufferMinutos(turno.getBufferMinutos());
        response.setPrecio(turno.getPrecio());
        response.setEstado(turno.getEstado().name());
        response.setClienteNombre(turno.getCliente().getNombre());
        response.setClienteTelefono(turno.getCliente().getTelefono());
        response.setClienteEmail(turno.getCliente().getEmail());
        response.setObservaciones(turno.getObservaciones());
        return response;
    }

    /**
     * Valida que el intervalo [horaInicio, horaFin) se encuentre dentro de al menos un rango de
     * disponibilidad configurado para el profesional en el día indicado.
     * Primero consulta DisponibilidadProfesional; si no hay, usa HorarioEmpresa (fallback).
     * Lanza ValidacionException si el horario está fuera de la disponibilidad configurada.
     */
    private void validarHorarioEnDisponibilidad(PerfilProfesional profesional, LocalDate fecha,
                                                LocalTime horaInicio, LocalTime horaFin,
                                                Empresa empresa) {
        DiaSemana dia = convertirDiaSemana(fecha.getDayOfWeek());

        java.util.List<DisponibilidadProfesional> disponibilidades =
            repositorioDisponibilidadProfesional.findByProfesionalAndDiaSemanaAndActivoTrue(profesional, dia);

        if (!disponibilidades.isEmpty()) {
            boolean dentroDe = disponibilidades.stream().anyMatch(d ->
                !horaInicio.isBefore(d.getHoraInicio()) && !horaFin.isAfter(d.getHoraFin()));
            if (!dentroDe) {
                throw new ValidacionException("El horario seleccionado está fuera de la disponibilidad del profesional");
            }
            return;
        }

        // Fallback: verificar contra horario de la empresa
        java.util.List<HorarioEmpresa> horariosEmpresa =
            repositorioHorarioEmpresa.findByEmpresaAndDiaSemanaAndActivoTrue(empresa, dia);
        if (horariosEmpresa.isEmpty()) {
            throw new ValidacionException("El profesional no tiene disponibilidad configurada para ese día");
        }
        boolean dentroDe = horariosEmpresa.stream().anyMatch(h ->
            !horaInicio.isBefore(h.getHoraInicio()) && !horaFin.isAfter(h.getHoraFin()));
        if (!dentroDe) {
            throw new ValidacionException("El horario seleccionado está fuera del horario de atención");
        }
    }

    private DiaSemana convertirDiaSemana(java.time.DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:    return DiaSemana.LUNES;
            case TUESDAY:   return DiaSemana.MARTES;
            case WEDNESDAY: return DiaSemana.MIERCOLES;
            case THURSDAY:  return DiaSemana.JUEVES;
            case FRIDAY:    return DiaSemana.VIERNES;
            case SATURDAY:  return DiaSemana.SABADO;
            case SUNDAY:    return DiaSemana.DOMINGO;
            default: throw new ValidacionException("Día de semana inválido");
        }
    }

    // ==================== MÉTODOS PARA PROFESIONAL ====================

    /**
     * Listar turnos del profesional con filtros
     */
    @Transactional(readOnly = true)
    public java.util.List<TurnoResponseProfesional> listarTurnosProfesional(
            String emailProfesional, 
            String fecha, 
            String fechaDesde, 
            String fechaHasta) {
        
        // Obtener profesional
        PerfilProfesional profesional = repositorioPerfilProfesional.findByUsuarioEmail(emailProfesional)
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        java.util.List<Turno> turnos;

        if (fecha != null) {
            // Filtrar por fecha específica
            // OPTIMIZADO: findByProfesionalWithDetails tiene JOIN FETCH → evita N+1
            LocalDate fechaFiltro = LocalDate.parse(fecha);
            turnos = repositorioTurno.findByProfesionalWithDetails(profesional, fechaFiltro, fechaFiltro);
        } else if (fechaDesde != null && fechaHasta != null) {
            // Filtrar por rango de fechas
            LocalDate desde = LocalDate.parse(fechaDesde);
            LocalDate hasta = LocalDate.parse(fechaHasta);
            turnos = repositorioTurno.findByProfesionalWithDetails(profesional, desde, hasta);
        } else {
            // Por defecto, semana actual — usa timezone de la empresa (no del servidor)
            LocalDate hoy = obtenerAhoraEmpresa(profesional.getEmpresa()).toLocalDate();
            LocalDate enUnaSemana = hoy.plusDays(7);
            turnos = repositorioTurno.findByProfesionalWithDetails(profesional, hoy, enUnaSemana);
        }

        return turnos.stream()
            .map(this::mapearATurnoResponseProfesional)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Cambiar estado de un turno
     */
    @Transactional
    public TurnoResponseProfesional cambiarEstadoTurno(
            String emailProfesional, 
            Long turnoId, 
            com.example.sitema_de_turnos.dto.CambiarEstadoTurnoRequest request) {
        
        // Obtener profesional
        PerfilProfesional profesional = repositorioPerfilProfesional.findByUsuarioEmail(emailProfesional)
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        // Obtener turno
        Turno turno = repositorioTurno.findById(turnoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        // Validar que el turno pertenece al profesional
        if (!turno.getProfesional().getId().equals(profesional.getId())) {
            throw new AccesoDenegadoException("No tienes permiso para modificar este turno");
        }

        // Validar transiciones de estado permitidas
        validarTransicionEstado(turno, request.getNuevoEstado());

        // Actualizar estado
        turno.setEstado(request.getNuevoEstado());
        
        // Si se proporcionaron observaciones, agregarlas
        if (request.getObservaciones() != null && !request.getObservaciones().trim().isEmpty()) {
            appendObservaciones(turno, request.getObservaciones());
        }

        turno = repositorioTurno.save(turno);
        return mapearATurnoResponseProfesional(turno);
    }

    /**
     * Agregar observaciones a un turno
     */
    @Transactional
    public TurnoResponseProfesional agregarObservacionesTurno(
            String emailProfesional, 
            Long turnoId, 
            com.example.sitema_de_turnos.dto.AgregarObservacionesRequest request) {
        
        // Obtener profesional
        PerfilProfesional profesional = repositorioPerfilProfesional.findByUsuarioEmail(emailProfesional)
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        // Obtener turno
        Turno turno = repositorioTurno.findById(turnoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        // Validar que el turno pertenece al profesional
        if (!turno.getProfesional().getId().equals(profesional.getId())) {
            throw new AccesoDenegadoException("No tienes permiso para modificar este turno");
        }

        // Agregar observaciones
        appendObservaciones(turno, request.getObservaciones());

        turno = repositorioTurno.save(turno);
        return mapearATurnoResponseProfesional(turno);
    }

    /**
     * Agrega texto a las observaciones existentes de un turno, separado por salto de línea.
     */
    private void appendObservaciones(Turno turno, String nueva) {
        String actual = turno.getObservaciones() != null ? turno.getObservaciones() : "";
        turno.setObservaciones(actual.isEmpty() ? nueva : actual + "\n" + nueva);
    }

    /**
     * Validar transiciones de estado permitidas
     */
    private void validarTransicionEstado(Turno turno, EstadoTurno nuevoEstado) {
        EstadoTurno estadoActual = turno.getEstado();
        
        // No se puede cambiar de CANCELADO o ATENDIDO
        if (estadoActual == EstadoTurno.CANCELADO) {
            throw new ValidacionException("No se puede modificar un turno cancelado");
        }
        if (estadoActual == EstadoTurno.ATENDIDO && nuevoEstado != EstadoTurno.ATENDIDO) {
            throw new ValidacionException("No se puede modificar un turno ya atendido");
        }

        // Validar transiciones específicas
        switch (nuevoEstado) {
            case PENDIENTE_CONFIRMACION:
                throw new ValidacionException("El estado PENDIENTE_CONFIRMACION no forma parte del flujo vigente de reservas");
            case PENDIENTE_PAGO:
                throw new ValidacionException("El estado PENDIENTE_PAGO se asigna automáticamente al crear reservas con seña");
            case CONFIRMADO:
                // Se puede confirmar desde PENDIENTE_PAGO
                if (estadoActual != EstadoTurno.PENDIENTE_PAGO) {
                    throw new ValidacionException("Solo se puede confirmar turnos en estado PENDIENTE_PAGO");
                }
                break;
            case ATENDIDO:
                // Se puede marcar como atendido desde CONFIRMADO
                if (estadoActual != EstadoTurno.CONFIRMADO) {
                    throw new ValidacionException("Solo se puede marcar como atendido turnos confirmados");
                }
                // Validar que el turno ya haya finalizado
                validarTurnoYaPaso(turno, "atendido");
                break;
            case NO_ASISTIO:
                // Se puede marcar NO_ASISTIO desde CONFIRMADO
                if (estadoActual != EstadoTurno.CONFIRMADO) {
                    throw new ValidacionException("Solo se puede marcar como no asistió turnos confirmados");
                }
                // Validar que el turno ya haya finalizado
                validarTurnoYaPaso(turno, "no asistió");
                break;
            case CANCELADO:
                // Se puede cancelar desde cualquier estado excepto ATENDIDO
                if (estadoActual == EstadoTurno.ATENDIDO) {
                    throw new ValidacionException("No se puede cancelar un turno ya atendido");
                }
                break;
        }
    }

    /**
     * Validar que un turno ya haya finalizado (fecha + horaFin < ahora).
     * Usa el timezone de la empresa para determinar la hora actual.
     */
    private void validarTurnoYaPaso(Turno turno, String accion) {
        ZonedDateTime ahoraEmpresa = obtenerAhoraEmpresa(turno.getEmpresa());
        LocalDateTime finTurno = LocalDateTime.of(turno.getFecha(), turno.getHoraFin());
        LocalDateTime ahoraLocal = ahoraEmpresa.toLocalDateTime();

        if (finTurno.isAfter(ahoraLocal)) {
            String fechaFormateada = turno.getFecha().format(FORMATTER_FECHA);
            String horaFormateada = turno.getHoraFin().format(FORMATTER_HORA);
            throw new ValidacionException(
                String.format("No se puede marcar como %s un turno que aún no ha finalizado. " +
                             "El turno finaliza el %s a las %s", 
                             accion, fechaFormateada, horaFormateada)
            );
        }
    }

    // ==================== MÉTODOS PARA CLIENTE ====================

    /**
     * Obtener todos los turnos del cliente autenticado (su historial completo).
     * Recibe el objeto Cliente ya validado por el SecurityContext → elimina el IDOR
     * que ocurría cuando se aceptaba un clienteId arbitrario como parámetro.
     * OPTIMIZADO: Usa JOIN FETCH para evitar N+1 queries.
     */
    @Transactional(readOnly = true)
    public java.util.List<TurnoResponsePublico> obtenerTurnosPorCliente(Cliente clienteAutenticado) {

        // OPTIMIZADO: Una sola query con JOIN FETCH en lugar de 1 + 3N queries
        java.util.List<Turno> turnos = repositorioTurno.findByClienteWithDetails(clienteAutenticado);

        return turnos.stream()
                .map(this::mapearATurnoResponsePublico)
                .collect(java.util.stream.Collectors.toList());
    }

    // ==================== MÉTODOS PRIVADOS PARA NOTIFICACIONES ====================

    /**
     * Enviar notificación de nuevo turno al profesional
     */
    private void enviarNotificacionNuevoTurno(Turno turno) {
        try {
            String titulo = "Nuevo turno asignado";
            String mensaje = String.format(
                "Nuevo turno: %s con %s para %s a las %s",
                turno.getServicio().getNombre(),
                turno.getCliente().getNombre(),
                turno.getFecha().format(FORMATTER_FECHA),
                turno.getHoraInicio().format(FORMATTER_HORA)
            );

            // ✅ Usa helper reutilizable en lugar de construir el mapa manualmente
            java.util.Map<String, Object> datos = construirDatosNotificacionBase(turno);
            datos.put("clienteTelefono", turno.getCliente().getTelefono());
            datos.put("clienteEmail", turno.getCliente().getEmail());
            datos.put("duracionMinutos", turno.getDuracionMinutos());
            datos.put("observaciones", turno.getObservaciones());
            datos.put("estado", turno.getEstado().name());

            servicioNotificacion.enviarNotificacion(
                turno.getProfesional().getId(),
                TipoNotificacion.NUEVO_TURNO,
                titulo,
                mensaje,
                datos,
                turno.getId()
            );
        } catch (Exception e) {
            log.error("Error al enviar notificación de nuevo turno", e);
            // No interrumpir el flujo principal si falla la notificación
        }
    }

    /**
     * Enviar notificación de cancelación al profesional
     */
    private void enviarNotificacionCancelacion(Turno turno, String canceladoPor) {
        try {
            TipoNotificacion tipo = "CLIENTE".equals(canceladoPor) 
                ? TipoNotificacion.CANCELACION_CLIENTE 
                : TipoNotificacion.CANCELACION_EMPRESA;

            String titulo = "CLIENTE".equals(canceladoPor) 
                ? "Turno cancelado por el cliente" 
                : "Turno cancelado por la empresa";

            String mensaje = String.format(
                "Turno cancelado: %s con %s previsto para %s a las %s. Motivo: %s",
                turno.getServicio().getNombre(),
                turno.getCliente().getNombre(),
                turno.getFecha().format(FORMATTER_FECHA),
                turno.getHoraInicio().format(FORMATTER_HORA),
                turno.getMotivoCancelacion() != null ? turno.getMotivoCancelacion() : "No especificado"
            );

            // ✅ Usa helper reutilizable
            java.util.Map<String, Object> datos = construirDatosNotificacionBase(turno);
            datos.put("motivoCancelacion", turno.getMotivoCancelacion());
            datos.put("canceladoPor", canceladoPor);
            datos.put("fechaCancelacion", turno.getFechaCancelacion().format(FORMATTER_DATETIME));

            servicioNotificacion.enviarNotificacion(
                turno.getProfesional().getId(),
                tipo,
                titulo,
                mensaje,
                datos,
                turno.getId()
            );
        } catch (Exception e) {
            log.error("Error al enviar notificación de cancelación", e);
        }
    }

    /**
     * Enviar notificación de reprogramación al profesional
     */
    private void enviarNotificacionReprogramacion(Turno turno, LocalDateTime fechaHoraAnterior, LocalDateTime fechaHoraNueva) {
        try {
            String titulo = "Turno reprogramado por el cliente";
            String mensaje = String.format(
                "Turno reprogramado: %s con %s. Anterior: %s a las %s. Nuevo: %s a las %s",
                turno.getServicio().getNombre(),
                turno.getCliente().getNombre(),
                fechaHoraAnterior.toLocalDate().format(FORMATTER_FECHA),
                fechaHoraAnterior.toLocalTime().format(FORMATTER_HORA),
                fechaHoraNueva.toLocalDate().format(FORMATTER_FECHA),
                fechaHoraNueva.toLocalTime().format(FORMATTER_HORA)
            );

            // ✅ M1: Datos extraídos a método reutilizable
            java.util.Map<String, Object> datos = construirDatosNotificacionBase(turno);
            
            // Datos específicos de reprogramación
            datos.put("fechaAnterior", fechaHoraAnterior.toLocalDate().toString());
            datos.put("horaAnterior", fechaHoraAnterior.toLocalTime().format(FORMATTER_HORA));
            datos.put("fechaNueva", turno.getFecha().toString());
            datos.put("horaNueva", turno.getHoraInicio().format(FORMATTER_HORA));

            servicioNotificacion.enviarNotificacion(
                turno.getProfesional().getId(),
                TipoNotificacion.REPROGRAMACION_CLIENTE,
                titulo,
                mensaje,
                datos,
                turno.getId()
            );
        } catch (Exception e) {
            log.error("Error al enviar notificación de reprogramación", e);
        }
    }

    /**
     * ✅ M1: Construir datos de notificación base para el frontend
     * Extrae la lógica duplicada de los 3 métodos de notificación
     */
    private java.util.Map<String, Object> construirDatosNotificacionBase(Turno turno) {
        java.util.Map<String, Object> datos = new java.util.HashMap<>();
        datos.put("turnoId", turno.getId());
        datos.put("clienteNombre", turno.getCliente().getNombre());
        datos.put("servicioNombre", turno.getServicio().getNombre());
        datos.put("fecha", turno.getFecha().toString());
        datos.put("horaInicio", turno.getHoraInicio().format(FORMATTER_HORA));
        datos.put("horaFin", turno.getHoraFin().format(FORMATTER_HORA));
        return datos;
    }
}
