package com.example.sitema_de_turnos.servicio;
import com.example.sitema_de_turnos.util.NormalizadorDatos;

import com.example.sitema_de_turnos.dto.publico.CrearTurnoRequest;
import com.example.sitema_de_turnos.dto.publico.TurnoResponsePublico;
import com.example.sitema_de_turnos.dto.publico.ReservaModificarRequest;
import com.example.sitema_de_turnos.dto.publico.TurnoResponseProfesional;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
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
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServicioTurno {

    private final RepositorioTurno repositorioTurno;
    private final RepositorioCliente repositorioCliente;
    private final RepositorioServicio repositorioServicio;
    private final RepositorioProfesional repositorioProfesional;
    private final RepositorioEmpresa repositorioEmpresa;
    private final RepositorioBloqueoFecha repositorioBloqueoFecha;

    /**
     * Crear un turno desde la vista pública.
     * ISOLATION.REPEATABLE_READ previene race conditions en la verificación de disponibilidad.
     * El constraint único en BD (idx_turno_no_overlap) es la defensa definitiva.
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
        Profesional profesional = repositorioProfesional.findById(request.getProfesionalId())
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
        if (tieneBloqueoDeFecha(profesional, fecha)) {
            throw new ValidacionException("El profesional no está disponible en la fecha seleccionada");
        }

        // 5. Validar que la fecha no sea pasada
        if (fecha.isBefore(LocalDate.now())) {
            throw new ValidacionException("No se puede reservar en una fecha pasada");
        }
        
        // 5.1. Validar tiempo mínimo de anticipación para el día actual
        if (fecha.isEqual(LocalDate.now())) {
            int tiempoMinimo = empresa.getTiempoMinimoAnticipacionMinutos() != null ? 
                empresa.getTiempoMinimoAnticipacionMinutos() : 30;
            LocalTime horaLimite = LocalTime.now().plusMinutes(tiempoMinimo);
            if (horaInicio.isBefore(horaLimite)) {
                throw new ValidacionException("Debe reservar con al menos " + tiempoMinimo + 
                    " minutos de anticipación. Próximo horario disponible: " + horaLimite.format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }

        // 6. Validar límite de días de reserva
        int diasMaximos = empresa.getDiasMaximosReserva() != null ? empresa.getDiasMaximosReserva() : 30;
        if (fecha.isAfter(LocalDate.now().plusDays(diasMaximos))) {
            throw new ValidacionException("No se puede reservar con más de " + diasMaximos + " días de anticipación");
        }

        // 7. Verificar disponibilidad (sin solapamiento)
        if (repositorioTurno.existeSolapamiento(profesional, fecha, horaInicio, horaFin)) {
            throw new ValidacionException("El horario seleccionado ya no está disponible");
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
        turno.setEstado(EstadoTurno.CREADO);
        turno.setObservaciones(request.getObservaciones());

        try {
            turno = repositorioTurno.save(turno);
        } catch (DataIntegrityViolationException e) {
            // Constraint único de BD detectó solapamiento (race condition perdida)
            throw new ValidacionException("El horario seleccionado ya no está disponible. Por favor, seleccione otro horario.");
        }

        // 10. Mapear respuesta PÚBLICA (sin exponer datos de otros clientes)
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
            throw new com.example.sitema_de_turnos.excepcion.AccesoDenegadoException("No tienes permiso para modificar este turno");
        }

        if (turno.getEstado() == EstadoTurno.CANCELADO || turno.getEstado() == EstadoTurno.ATENDIDO) {
            throw new ValidacionException("No se puede modificar un turno cancelado o ya atendido");
        }

        // Validar que el turno no sea pasado ni esté muy cerca (menos de 1 hora)
        LocalDateTime inicioTurno = LocalDateTime.of(turno.getFecha(), turno.getHoraInicio());
        LocalDateTime ahora = LocalDateTime.now();

        if (inicioTurno.isBefore(ahora)) {
            log.info("Rechazando modificación: turno pasado turnoId={}, inicio={}, ahora={}", turnoId, inicioTurno, ahora);
            throw new ValidacionException("No se puede modificar un turno que ya pasó");
        }

        if (inicioTurno.minusHours(1).isBefore(ahora)) {
            log.info("Rechazando modificación: poca anticipación turnoId={}, inicio={}, ahora={}", turnoId, inicioTurno, ahora);
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
     */
    @Transactional
    public TurnoResponsePublico reprogramarReservaPorCliente(Long turnoId, Cliente cliente, com.example.sitema_de_turnos.dto.publico.ReservaReprogramarRequest request) {
        Turno turno = repositorioTurno.findById(turnoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        log.info("reprogramarReservaPorCliente called: turnoId={}, turnoClienteId={}, requesterClienteId={}", turnoId, turno.getCliente().getId(), cliente.getId());

        if (!turno.getCliente().getId().equals(cliente.getId())) {
            throw new com.example.sitema_de_turnos.excepcion.AccesoDenegadoException("No tienes permiso para reprogramar este turno");
        }

        if (turno.getEstado() == EstadoTurno.CANCELADO || turno.getEstado() == EstadoTurno.ATENDIDO) {
            throw new ValidacionException("No se puede reprogramar un turno cancelado o ya atendido");
        }

        // Validar que el turno origen no sea pasado ni esté muy cerca (menos de 1 hora)
        LocalDateTime inicioTurno = LocalDateTime.of(turno.getFecha(), turno.getHoraInicio());
        LocalDateTime ahora = LocalDateTime.now();

        if (inicioTurno.isBefore(ahora)) {
            throw new ValidacionException("No se puede reprogramar un turno que ya pasó");
        }

        if (inicioTurno.minusHours(1).isBefore(ahora)) {
            throw new ValidacionException("No se puede reprogramar un turno con menos de 1 hora de anticipación");
        }

        // Campos solicitados (parse desde strings para compatibilidad de serialización en tests)
        java.time.LocalDate nuevaFecha = request.getFechaAsLocalDate();
        java.time.LocalTime nuevaHoraInicio = request.getHoraInicioAsLocalTime();
        Long nuevoProfesionalId = request.getProfesionalId();

        if (nuevaFecha == null || nuevaHoraInicio == null) {
            throw new ValidacionException("Fecha y hora inicio son requeridos para reprogramar");
        }

        // Validar fecha no pasada
        if (nuevaFecha.isBefore(LocalDate.now())) {
            throw new ValidacionException("No se puede reprogramar a una fecha pasada");
        }

        // Determinar profesional objetivo
        Profesional profesionalDestino;
        if (nuevoProfesionalId != null) {
            profesionalDestino = repositorioProfesional.findById(nuevoProfesionalId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));
            if (!profesionalDestino.getActivo() || !profesionalDestino.getEmpresa().getId().equals(turno.getEmpresa().getId())) {
                throw new ValidacionException("Profesional no disponible");
            }
        } else {
            profesionalDestino = turno.getProfesional();
        }

        // Validar bloqueos de fecha del profesional destino
        if (tieneBloqueoDeFecha(profesionalDestino, nuevaFecha)) {
            throw new ValidacionException("El profesional no está disponible en la fecha seleccionada");
        }

        // Calcular hora fin usando la duración y buffer del turno actual
        LocalTime nuevaHoraFin = nuevaHoraInicio.plusMinutes(turno.getDuracionMinutos()).plusMinutes(turno.getBufferMinutos());

        // Validar disponibilidad: buscar turnos activos del profesional en esa fecha y comprobar solapamientos (excluyendo este turno)
        java.util.List<Turno> turnosProfesional = repositorioTurno.findTurnosActivosByProfesionalAndFecha(profesionalDestino, nuevaFecha);
        for (Turno t : turnosProfesional) {
            if (t.getId().equals(turno.getId())) continue;
            if (t.getHoraInicio().isBefore(nuevaHoraFin) && t.getHoraFin().isAfter(nuevaHoraInicio)) {
                throw new ValidacionException("El horario seleccionado ya no está disponible");
            }
        }

        // Si todo OK, aplicar cambios
        turno.setProfesional(profesionalDestino);
        turno.setFecha(nuevaFecha);
        turno.setHoraInicio(nuevaHoraInicio);
        turno.setHoraFin(nuevaHoraFin);

        turno = repositorioTurno.save(turno);

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
            throw new com.example.sitema_de_turnos.excepcion.AccesoDenegadoException("No tienes permiso para cancelar este turno");
        }

        // Delega en la lógica existente que valida tiempos y estados
        cancelarTurno(turnoId, motivo, "CLIENTE");
    }

    /**
     * Verificar si el profesional tiene bloqueo en una fecha específica
     */
    private boolean tieneBloqueoDeFecha(Profesional profesional, LocalDate fecha) {
        java.util.List<BloqueoFecha> bloqueos = repositorioBloqueoFecha.findByProfesionalAndActivoTrue(profesional);
        
        return bloqueos.stream()
                .anyMatch(bloqueo -> {
                    LocalDate inicio = bloqueo.getFechaInicio();
                    LocalDate fin = bloqueo.getFechaFin() != null ? bloqueo.getFechaFin() : inicio;
                    return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
                });
    }

    /**
     * Obtener cliente existente o crear uno nuevo
     */
    private Cliente obtenerOCrearCliente(Empresa empresa, CrearTurnoRequest request) {
        // Buscar por teléfono
        Optional<Cliente> clienteExistente = repositorioCliente.findByEmpresaAndTelefonoAndActivoTrue(
            empresa, 
            request.getTelefonoCliente()
        );

        if (clienteExistente.isPresent()) {
            Cliente c = clienteExistente.get();
            // Si el cliente ya tiene cuenta registrada, NO se puede asociar automáticamente
            if (Boolean.TRUE.equals(c.getTieneUsuario())) {
                throw new com.example.sitema_de_turnos.excepcion.ConflictoException(
                    "El teléfono pertenece a una cuenta registrada. Por favor, iniciá sesión o usá otro número."
                );
            }
            return c; // cliente invitado -> asociar
        }

        // Crear nuevo cliente invitado
        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setEmpresa(empresa);
            nuevoCliente.setNombre(NormalizadorDatos.normalizarNombre(request.getNombreCliente()));
        nuevoCliente.setTelefono(request.getTelefonoCliente());
        
        // Solo setear email si no está vacío
        String email = request.getEmailCliente();
        if (email != null && !email.trim().isEmpty()) {
                nuevoCliente.setEmail(NormalizadorDatos.normalizarEmail(email));
        }
        
        nuevoCliente.setTieneUsuario(false);
        nuevoCliente.setTelefonoValidado(false);
        nuevoCliente.setActivo(true);

        try {
            return repositorioCliente.save(nuevoCliente);
        } catch (DataIntegrityViolationException e) {
            // Otro thread creó el cliente simultáneamente, reintentamos la búsqueda
            return repositorioCliente.findByEmpresaAndTelefonoAndActivoTrue(empresa, request.getTelefonoCliente())
                    .orElseThrow(() -> new ValidacionException("Error al crear el cliente. Intente nuevamente."));
        }
    }

    /**
     * Cancelar turno con validación de anticipación mínima (1 hora)
     */
    @Transactional
    public void cancelarTurno(Long turnoId, String motivo, String canceladoPor) {
        Turno turno = repositorioTurno.findById(turnoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        if (turno.getEstado() == EstadoTurno.CANCELADO) {
            throw new ValidacionException("El turno ya está cancelado");
        }

        if (turno.getEstado() == EstadoTurno.ATENDIDO) {
            throw new ValidacionException("No se puede cancelar un turno ya atendido");
        }

        // Validar que el turno no sea pasado y que la cancelación respete la anticipación mínima (1 hora)
        LocalDateTime inicioTurno = LocalDateTime.of(turno.getFecha(), turno.getHoraInicio());
        LocalDateTime ahora = LocalDateTime.now();

        if (inicioTurno.isBefore(ahora)) {
            throw new ValidacionException("No se puede cancelar un turno que ya pasó");
        }

        if (inicioTurno.minusHours(1).isBefore(ahora)) {
            throw new ValidacionException(
                "No se puede cancelar con menos de 1 hora de anticipación. " +
                "Por favor, comuníquese con la empresa al teléfono: " + 
                turno.getEmpresa().getTelefono()
            );
        }

        turno.setEstado(EstadoTurno.CANCELADO);
        turno.setMotivoCancelacion(motivo);
        turno.setCanceladoPor(canceladoPor);
        turno.setFechaCancelacion(LocalDateTime.now());

        repositorioTurno.save(turno);
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
        response.setProfesionalNombre(turno.getProfesional().getNombre());
        response.setProfesionalApellido(turno.getProfesional().getApellido());
        response.setFecha(turno.getFecha().toString());
        response.setHoraInicio(turno.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")));
        response.setDuracionMinutos(turno.getDuracionMinutos());
        response.setPrecio(turno.getPrecio());
        response.setEstado(turno.getEstado().name());
        response.setObservaciones(turno.getObservaciones());
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
        response.setProfesionalNombre(turno.getProfesional().getNombre());
        response.setProfesionalApellido(turno.getProfesional().getApellido());
        response.setFecha(turno.getFecha().toString());
        response.setHoraInicio(turno.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")));
        response.setHoraFin(turno.getHoraFin().format(DateTimeFormatter.ofPattern("HH:mm")));
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
        Profesional profesional = repositorioProfesional.findByEmail(emailProfesional)
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        java.util.List<Turno> turnos;

        if (fecha != null) {
            // Filtrar por fecha específica
            LocalDate fechaFiltro = LocalDate.parse(fecha);
            turnos = repositorioTurno.findTurnosActivosByProfesionalAndFecha(profesional, fechaFiltro);
        } else if (fechaDesde != null && fechaHasta != null) {
            // Filtrar por rango de fechas
            LocalDate desde = LocalDate.parse(fechaDesde);
            LocalDate hasta = LocalDate.parse(fechaHasta);
            turnos = repositorioTurno.findByProfesionalAndFechaBetweenOrderByFechaAscHoraInicioAsc(
                profesional, desde, hasta);
        } else {
            // Por defecto, mostrar turnos de hoy en adelante
            LocalDate hoy = LocalDate.now();
            LocalDate enUnaSemana = hoy.plusDays(7);
            turnos = repositorioTurno.findByProfesionalAndFechaBetweenOrderByFechaAscHoraInicioAsc(
                profesional, hoy, enUnaSemana);
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
        Profesional profesional = repositorioProfesional.findByEmail(emailProfesional)
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        // Obtener turno
        Turno turno = repositorioTurno.findById(turnoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        // Validar que el turno pertenece al profesional
        if (!turno.getProfesional().getId().equals(profesional.getId())) {
            throw new ValidacionException("No tienes permiso para modificar este turno");
        }

        // Validar transiciones de estado permitidas
        validarTransicionEstado(turno.getEstado(), request.getNuevoEstado());

        // Actualizar estado
        turno.setEstado(request.getNuevoEstado());
        
        // Si se proporcionaron observaciones, agregarlas
        if (request.getObservaciones() != null && !request.getObservaciones().trim().isEmpty()) {
            String observacionesActuales = turno.getObservaciones() != null ? turno.getObservaciones() : "";
            String nuevasObservaciones = observacionesActuales.isEmpty() 
                ? request.getObservaciones()
                : observacionesActuales + "\n" + request.getObservaciones();
            turno.setObservaciones(nuevasObservaciones);
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
        Profesional profesional = repositorioProfesional.findByEmail(emailProfesional)
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional no encontrado"));

        // Obtener turno
        Turno turno = repositorioTurno.findById(turnoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado"));

        // Validar que el turno pertenece al profesional
        if (!turno.getProfesional().getId().equals(profesional.getId())) {
            throw new ValidacionException("No tienes permiso para modificar este turno");
        }

        // Agregar observaciones
        String observacionesActuales = turno.getObservaciones() != null ? turno.getObservaciones() : "";
        String nuevasObservaciones = observacionesActuales.isEmpty() 
            ? request.getObservaciones()
            : observacionesActuales + "\n" + request.getObservaciones();
        turno.setObservaciones(nuevasObservaciones);

        turno = repositorioTurno.save(turno);
        return mapearATurnoResponseProfesional(turno);
    }

    /**
     * Validar transiciones de estado permitidas
     */
    private void validarTransicionEstado(EstadoTurno estadoActual, EstadoTurno nuevoEstado) {
        // No se puede cambiar de CANCELADO o ATENDIDO
        if (estadoActual == EstadoTurno.CANCELADO) {
            throw new ValidacionException("No se puede modificar un turno cancelado");
        }
        if (estadoActual == EstadoTurno.ATENDIDO && nuevoEstado != EstadoTurno.ATENDIDO) {
            throw new ValidacionException("No se puede modificar un turno ya atendido");
        }

        // Validar transiciones específicas
        switch (nuevoEstado) {
            case CREADO:
                // No tiene sentido cambiar a CREADO (es el estado inicial)
                throw new ValidacionException("No se puede cambiar el estado a CREADO");
            case PENDIENTE_CONFIRMACION:
                // Se puede marcar como pendiente de confirmación desde CREADO
                if (estadoActual != EstadoTurno.CREADO) {
                    throw new ValidacionException("Solo se puede marcar como pendiente de confirmación turnos en estado CREADO");
                }
                break;
            case CONFIRMADO:
                // Se puede confirmar desde CREADO o PENDIENTE_CONFIRMACION
                if (estadoActual != EstadoTurno.CREADO && estadoActual != EstadoTurno.PENDIENTE_CONFIRMACION) {
                    throw new ValidacionException("Solo se puede confirmar turnos en estado CREADO o PENDIENTE_CONFIRMACION");
                }
                break;
            case ATENDIDO:
                // Se puede marcar como atendido desde CONFIRMADO
                if (estadoActual != EstadoTurno.CONFIRMADO) {
                    throw new ValidacionException("Solo se puede marcar como atendido turnos confirmados");
                }
                break;
            case NO_ASISTIO:
                // Se puede marcar NO_ASISTIO desde CONFIRMADO
                if (estadoActual != EstadoTurno.CONFIRMADO) {
                    throw new ValidacionException("Solo se puede marcar como no asistió turnos confirmados");
                }
                break;
            case CANCELADO:
                // Se puede cancelar desde cualquier estado excepto ATENDIDO
                if (estadoActual == EstadoTurno.ATENDIDO) {
                    throw new ValidacionException("No se puede cancelar un turno ya atendido");
                }
                break;
        }
    }

    // ==================== MÉTODOS PARA CLIENTE ====================

    /**
     * Obtener todos los turnos de un cliente (su historial completo)
     */
    @Transactional(readOnly = true)
    public java.util.List<TurnoResponsePublico> obtenerTurnosPorCliente(Long clienteId) {
        
        Cliente cliente = repositorioCliente.findById(clienteId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));

        java.util.List<Turno> turnos = repositorioTurno.findByClienteOrderByFechaDescHoraInicioDesc(cliente);

        return turnos.stream()
                .map(this::mapearATurnoResponsePublico)
                .collect(java.util.stream.Collectors.toList());
    }
}
