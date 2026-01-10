package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.HorarioEmpresaRequest;
import com.example.sitema_de_turnos.dto.HorarioEmpresaResponse;
import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.modelo.Dueno;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.HorarioEmpresa;
import com.example.sitema_de_turnos.repositorio.RepositorioHorarioEmpresa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioHorarioEmpresa {

    private final RepositorioHorarioEmpresa repositorioHorarioEmpresa;
    private final ServicioValidacionDueno servicioValidacionDueno;

    @Transactional
    public HorarioEmpresaResponse crearHorario(String emailDueno, HorarioEmpresaRequest request) {
        // Validar horarios
        if (request.getHoraFin().isBefore(request.getHoraInicio()) || 
            request.getHoraFin().equals(request.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // Validar dueño y obtener empresa
        Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);
        Empresa empresa = dueno.getEmpresa();

        // Verificar solapamientos con horarios existentes del mismo día
        List<HorarioEmpresa> horariosExistentes = 
                repositorioHorarioEmpresa.findByEmpresaAndDiaSemanaAndActivoTrue(empresa, request.getDiaSemana());

        validarNoSolapamiento(horariosExistentes, request.getHoraInicio(), request.getHoraFin(), null);

        // Crear horario
        HorarioEmpresa horario = new HorarioEmpresa();
        horario.setEmpresa(empresa);
        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        horario.setActivo(true);

        horario = repositorioHorarioEmpresa.save(horario);

        return convertirAResponse(horario);
    }

    @Transactional
    public HorarioEmpresaResponse actualizarHorario(String emailDueno, Long horarioId, HorarioEmpresaRequest request) {
        // Validar horarios
        if (request.getHoraFin().isBefore(request.getHoraInicio()) || 
            request.getHoraFin().equals(request.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // Validar dueño y obtener empresa
        Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);
        Empresa empresa = dueno.getEmpresa();

        // Obtener horario y validar pertenencia
        HorarioEmpresa horario = repositorioHorarioEmpresa.findById(horarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Horario no encontrado"));

        if (!horario.getEmpresa().getId().equals(empresa.getId())) {
            throw new AccesoDenegadoException("No tiene permiso para modificar este horario");
        }

        // Verificar solapamientos (excluyendo el horario actual)
        List<HorarioEmpresa> horariosExistentes = 
                repositorioHorarioEmpresa.findByEmpresaAndDiaSemanaAndActivoTrue(empresa, request.getDiaSemana());

        validarNoSolapamiento(horariosExistentes, request.getHoraInicio(), request.getHoraFin(), horarioId);

        // Actualizar horario
        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());

        horario = repositorioHorarioEmpresa.save(horario);

        return convertirAResponse(horario);
    }

    @Transactional(readOnly = true)
    public List<HorarioEmpresaResponse> obtenerHorariosPorEmpresa(String emailDueno) {
        // Validar dueño y obtener empresa
        Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);
        Empresa empresa = dueno.getEmpresa();

        List<HorarioEmpresa> horarios = repositorioHorarioEmpresa.findByEmpresaAndActivoTrue(empresa);

        return horarios.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Elimina un horario de la empresa.
     * Reglas de eliminación:
     * - Si NO tiene turnos asociados → DELETE físico de la base de datos
     * - Si tiene turnos terminados (pasados) → Desactivar (soft delete: activo = false)
     * - Si tiene turnos actuales/futuros → No permitir eliminación (lanzar excepción)
     *
     * NOTA: Cuando se implemente el modelo Turno, descomentar la lógica de validación.
     */
    @Transactional
    public void eliminarHorario(String emailDueno, Long horarioId) {
        // Validar dueño y empresa
        Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);
        Empresa empresa = dueno.getEmpresa();

        // Obtener horario y validar pertenencia
        HorarioEmpresa horario = repositorioHorarioEmpresa.findById(horarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Horario no encontrado"));

        if (!horario.getEmpresa().getId().equals(empresa.getId())) {
            throw new AccesoDenegadoException("No tiene permiso para eliminar este horario");
        }

        // TODO: Cuando exista el modelo Turno, descomentar esta validación:
        /*
        List<Turno> turnos = repositorioTurno.findByHorarioEmpresaId(horarioId);
        
        if (!turnos.isEmpty()) {
            // Verificar si hay turnos futuros o actuales (pendientes)
            LocalDateTime ahora = LocalDateTime.now();
            boolean hasTurnosFuturos = turnos.stream()
                    .anyMatch(t -> t.getEstado() != EstadoTurno.COMPLETADO 
                                && t.getEstado() != EstadoTurno.CANCELADO
                                && t.getFechaHora().isAfter(ahora));
            
            if (hasTurnosFuturos) {
                throw new IllegalStateException(
                    "No se puede eliminar el horario porque tiene turnos pendientes o futuros. " +
                    "Debe cancelar o completar los turnos primero."
                );
            }
            
            // Si solo tiene turnos pasados/completados, hacer soft delete
            horario.setActivo(false);
            repositorioHorarioEmpresa.save(horario);
            return;
        }
        */
        
        // Mientras no exista Turno, hacer DELETE físico directamente
        // (Una vez implementado Turno, este código solo se ejecuta si no hay turnos asociados)
        repositorioHorarioEmpresa.delete(horario);
    }

    /**
     * Valida que no haya solapamiento entre horarios.
     * Dos horarios se solapan si:
     * - El nuevo inicio está dentro de un horario existente
     * - El nuevo fin está dentro de un horario existente
     * - El nuevo horario envuelve completamente uno existente
     * - Los horarios son exactamente iguales
     */
    private void validarNoSolapamiento(List<HorarioEmpresa> horariosExistentes, 
                                       java.time.LocalTime horaInicio, 
                                       java.time.LocalTime horaFin, 
                                       Long horarioIdExcluir) {
        for (HorarioEmpresa existente : horariosExistentes) {
            // Excluir el horario actual si estamos actualizando
            if (horarioIdExcluir != null && existente.getId().equals(horarioIdExcluir)) {
                continue;
            }

            boolean overlap = 
                // Nuevo inicio está dentro del rango existente
                (horaInicio.isAfter(existente.getHoraInicio()) && horaInicio.isBefore(existente.getHoraFin())) ||
                // Nuevo fin está dentro del rango existente
                (horaFin.isAfter(existente.getHoraInicio()) && horaFin.isBefore(existente.getHoraFin())) ||
                // Nuevo horario envuelve al existente
                (horaInicio.isBefore(existente.getHoraInicio()) && horaFin.isAfter(existente.getHoraFin())) ||
                // Horarios exactamente iguales
                (horaInicio.equals(existente.getHoraInicio()) || horaFin.equals(existente.getHoraFin()));

            if (overlap) {
                throw new IllegalArgumentException(
                    String.format("El horario se solapa con un horario existente: %s - %s",
                        existente.getHoraInicio(),
                        existente.getHoraFin()));
            }
        }
    }

    /**
     * Copia todos los horarios de un día fuente a uno o más días destino.
     * 
     * @param diaFuente Día desde el cual copiar los horarios
     * @param diasDestino Lista de días a los cuales copiar
     * @param reemplazar Si true, elimina horarios existentes en días destino antes de copiar.
     *                   Si false y hay horarios existentes, lanza excepción.
     * @return Número de horarios creados
     */
    @Transactional
    public int copiarHorariosAOtrosDias(String emailDueno, 
                                        com.example.sitema_de_turnos.modelo.DiaSemana diaFuente, 
                                        List<com.example.sitema_de_turnos.modelo.DiaSemana> diasDestino,
                                        boolean reemplazar) {
        // Validar dueño y obtener empresa
        Dueno dueno = servicioValidacionDueno.validarYObtenerDueno(emailDueno);
        Empresa empresa = dueno.getEmpresa();

        // Obtener horarios del día fuente
        List<HorarioEmpresa> horariosFuente = 
                repositorioHorarioEmpresa.findByEmpresaAndDiaSemanaAndActivoTrue(empresa, diaFuente);

        if (horariosFuente.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("El día %s no tiene horarios configurados para copiar", diaFuente));
        }

        int horariosCreados = 0;

        // Procesar cada día destino
        for (com.example.sitema_de_turnos.modelo.DiaSemana diaDestino : diasDestino) {
            // Verificar si el día destino ya tiene horarios
            List<HorarioEmpresa> horariosExistentes = 
                    repositorioHorarioEmpresa.findByEmpresaAndDiaSemanaAndActivoTrue(empresa, diaDestino);

            if (!horariosExistentes.isEmpty()) {
                if (!reemplazar) {
                    throw new IllegalArgumentException(
                        String.format("El día %s ya tiene horarios configurados. " +
                            "Confirme el reemplazo para continuar.", diaDestino));
                }
                // Eliminar horarios existentes (soft delete)
                for (HorarioEmpresa existente : horariosExistentes) {
                    existente.setActivo(false);
                    repositorioHorarioEmpresa.save(existente);
                }
            }

            // Copiar cada horario del día fuente al día destino
            for (HorarioEmpresa horarioFuente : horariosFuente) {
                HorarioEmpresa nuevoHorario = new HorarioEmpresa();
                nuevoHorario.setEmpresa(empresa);
                nuevoHorario.setDiaSemana(diaDestino);
                nuevoHorario.setHoraInicio(horarioFuente.getHoraInicio());
                nuevoHorario.setHoraFin(horarioFuente.getHoraFin());
                nuevoHorario.setActivo(true);
                repositorioHorarioEmpresa.save(nuevoHorario);
                horariosCreados++;
            }
        }

        return horariosCreados;
    }

    /**
     * Obtiene los horarios de la empresa del profesional para visualización de referencia.
     * Este método es de solo lectura y no debe usarse para cálculo de disponibilidad.
     */
    @Transactional(readOnly = true)
    public List<HorarioEmpresaResponse> obtenerHorariosPorProfesional(com.example.sitema_de_turnos.modelo.Profesional profesional) {
        Empresa empresa = profesional.getEmpresa();
        List<HorarioEmpresa> horarios = repositorioHorarioEmpresa.findByEmpresaAndActivoTrue(empresa);
        return horarios.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    private HorarioEmpresaResponse convertirAResponse(HorarioEmpresa horario) {
        HorarioEmpresaResponse response = new HorarioEmpresaResponse();
        response.setId(horario.getId());
        response.setEmpresaId(horario.getEmpresa().getId());
        response.setDiaSemana(horario.getDiaSemana());
        response.setHoraInicio(horario.getHoraInicio());
        response.setHoraFin(horario.getHoraFin());
        response.setActivo(horario.getActivo());
        return response;
    }
}
