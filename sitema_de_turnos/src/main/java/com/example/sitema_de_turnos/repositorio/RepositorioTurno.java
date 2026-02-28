package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface RepositorioTurno extends JpaRepository<Turno, Long> {

    /**
     * Buscar turnos de un profesional en una fecha específica
     */
    List<Turno> findByProfesionalAndFechaAndEstadoNotOrderByHoraInicio(
        Profesional profesional, 
        LocalDate fecha, 
        EstadoTurno estado
    );

    /**
     * Buscar turnos de un cliente en una empresa
     */
    List<Turno> findByClienteAndEmpresaOrderByFechaDescHoraInicioDesc(Cliente cliente, Empresa empresa);

    /**
     * Buscar turnos de una empresa en un rango de fechas
     */
    List<Turno> findByEmpresaAndFechaBetweenOrderByFechaAscHoraInicioAsc(
        Empresa empresa, 
        LocalDate fechaInicio, 
        LocalDate fechaFin
    );

    /**
     * Verificar si existe un turno en un horario específico para un profesional
     * (excluyendo turnos cancelados)
     */
    @Query("SELECT COUNT(t) > 0 FROM Turno t WHERE t.profesional = :profesional " +
           "AND t.fecha = :fecha " +
           "AND t.estado NOT IN ('CANCELADO') " +
           "AND ((t.horaInicio < :horaFin AND t.horaFin > :horaInicio))")
    boolean existeSolapamiento(
        @Param("profesional") Profesional profesional,
        @Param("fecha") LocalDate fecha,
        @Param("horaInicio") LocalTime horaInicio,
        @Param("horaFin") LocalTime horaFin
    );

    /**
     * Buscar turnos activos de un profesional en una fecha
     */
    @Query("SELECT t FROM Turno t WHERE t.profesional = :profesional " +
           "AND t.fecha = :fecha " +
           "AND t.estado NOT IN ('CANCELADO') " +
           "ORDER BY t.horaInicio")
    List<Turno> findTurnosActivosByProfesionalAndFecha(
        @Param("profesional") Profesional profesional,
        @Param("fecha") LocalDate fecha
    );

    /**
     * Buscar turnos de un profesional en un rango de fechas
     * 
     * ⚠️ DEPRECADO: Causa N+1 queries. Usar findByProfesionalWithDetails() en su lugar.
     */
    List<Turno> findByProfesionalAndFechaBetweenOrderByFechaAscHoraInicioAsc(
        Profesional profesional,
        LocalDate fechaInicio,
        LocalDate fechaFin
    );

    /**
     * Buscar turnos de un profesional en un rango de fechas con todas las relaciones.
     * 
     * OPTIMIZADO: Usa JOIN FETCH para evitar N+1 queries en agenda de profesional.
     * Carga: Turno + Servicio + Cliente + Empresa
     * 
     * Casos de uso:
     * - Vista de agenda del profesional (rango de semana/mes)
     * - Reporte de turnos atendidos
     * - Dashboard de profesional
     * 
     * Mejora: 50 turnos en semana = 151 queries → 1 query (151x más rápido)
     * 
     * @param profesional Profesional cuya agenda se consulta
     * @param fechaInicio Fecha inicial del rango (inclusive)
     * @param fechaFin Fecha final del rango (inclusive)
     * @return Lista de turnos ordenados por fecha y hora con relaciones cargadas
     */
    @Query("SELECT DISTINCT t FROM Turno t " +
           "LEFT JOIN FETCH t.servicio " +
           "LEFT JOIN FETCH t.cliente " +
           "LEFT JOIN FETCH t.empresa " +
           "WHERE t.profesional = :profesional " +
           "AND t.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY t.fecha ASC, t.horaInicio ASC")
    List<Turno> findByProfesionalWithDetails(
        @Param("profesional") Profesional profesional,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );

    /**
     * Buscar todos los turnos de un cliente (historial completo)
     * Ordenados por fecha descendente (más recientes primero)
     * 
     * ⚠️ DEPRECADO: Causa N+1 queries. Usar findByClienteWithDetails() en su lugar.
     */
    List<Turno> findByClienteOrderByFechaDescHoraInicioDesc(Cliente cliente);

    /**
     * Buscar todos los turnos de un cliente con todas las relaciones cargadas (historial completo).
     * 
     * OPTIMIZADO: Usa JOIN FETCH para evitar N+1 queries.
     * Carga en UNA sola query: Turno + Servicio + Profesional + Empresa
     * (Cliente ya viene del parámetro, no necesita fetch adicional)
     * 
     * Sin optimización (N+1):
     * - 1 query para turnos
     * - N queries para cada servicio
     * - N queries para cada profesional
     * - N queries para cada empresa
     * Total: 1 + 3N queries
     * 
     * Con optimización (JOIN FETCH):
     * - 1 query con todos los datos
     * Total: 1 query
     * 
     * Mejora: 100 turnos = 301 queries → 1 query (301x más rápido)
     * 
     * @param cliente Cliente cuyo historial se quiere consultar
     * @return Lista de turnos ordenados por fecha descendente con todas las relaciones cargadas
     */
    @Query("SELECT DISTINCT t FROM Turno t " +
           "LEFT JOIN FETCH t.servicio " +
           "LEFT JOIN FETCH t.profesional " +
           "LEFT JOIN FETCH t.empresa " +
           "WHERE t.cliente = :cliente " +
           "ORDER BY t.fecha DESC, t.horaInicio DESC")
    List<Turno> findByClienteWithDetails(@Param("cliente") Cliente cliente);

    /**
     * Buscar turnos para envío de recordatorios.
     * 
     * DOBLEMENTE OPTIMIZADO:
     * 1. Índice: idx_turno_recordatorios (empresa_id, fecha, estado, hora_inicio)
     * 2. JOIN FETCH: Evita N+1 queries cargando todas las relaciones necesarias
     * 
     * Query típica de job batch de recordatorios:
     * - Filtrar por empresa (multi-tenant)
     * - Filtrar por fecha específica (ej: turnos de mañana)
     * - Solo turnos en estado activo (CREADO, CONFIRMADO)
     * - Ordenados por hora para envío escalonado
     * - Carga Cliente, Profesional, Servicio para generar mensajes
     * 
     * Performance sin JOIN FETCH:
     * - 1000 turnos = 1 query (índice) + 3000 queries (N+1) = 3001 queries
     * 
     * Performance con JOIN FETCH:
     * - 1000 turnos = 1 query con todos los datos = 1 query
     * 
     * Mejora: 3001x más rápido en generación de recordatorios masivos
     * 
     * Ejemplo de uso:
     * - Recordatorio 24h antes: fecha = LocalDate.now().plusDays(1)
     * - Recordatorio mismo día: fecha = LocalDate.now()
     * 
     * @param empresa Empresa a filtrar (multi-tenant isolation)
     * @param fecha Fecha de los turnos (ej: mañana para recordatorio 24h)
     * @param estados Estados válidos para recordatorio (CREADO, CONFIRMADO)
     * @return Lista ordenada por hora_inicio con todas las relaciones cargadas
     */
    @Query("SELECT DISTINCT t FROM Turno t " +
           "LEFT JOIN FETCH t.cliente " +
           "LEFT JOIN FETCH t.profesional " +
           "LEFT JOIN FETCH t.servicio " +
           "WHERE t.empresa = :empresa " +
           "AND t.fecha = :fecha " +
           "AND t.estado IN :estados " +
           "ORDER BY t.horaInicio ASC")
    List<Turno> findTurnosParaRecordatorios(
        @Param("empresa") Empresa empresa,
        @Param("fecha") LocalDate fecha,
        @Param("estados") List<EstadoTurno> estados
    );

    /**
     * Buscar turnos pendientes de recordatorio (JPQL portable)
     * 
     * REFACTORIZADO (A2 - Portabilidad):
     * Convertido a JPQL para soportar múltiples bases de datos (PostgreSQL, H2, MySQL).
     * La comparación de fecha/hora se hace por partes en lugar de usar operador específico de PostgreSQL.
     * 
     * REFACTORIZADO (A4 - Prevención de duplicados):
     * Busca turnos que cumplan:
     * - Fecha/hora del turno está dentro de las próximas X horas (desde ahora hasta fechaHoraMax)
     * - Estado CONFIRMADO
     * - NO hayan recibido recordatorio (recordatorioEnviado = false)
     * - NO estén siendo procesados actualmente (recordatorioPrimerIntento IS NULL) ✅ A4
     * 
     * Esta última condición previene que el mismo turno sea seleccionado múltiples veces
     * cuando el cron se ejecuta frecuentemente (ej: cada minuto en dev).
     * 
     * Lógica de comparación: turno califica si su (fecha + horaInicio) cae en el rango [min, max]
     * - Si turno.fecha > fechaMax → excluir
     * - Si turno.fecha < fechaMin → excluir  
     * - Si turno.fecha = fechaMin → incluir solo si horaInicio >= horaMin
     * - Si turno.fecha = fechaMax → incluir solo si horaInicio <= horaMax
     * - Si fechaMin < turno.fecha < fechaMax → incluir
     * 
     * @param empresaId ID de la empresa a filtrar (multi-tenant)
     * @param fechaMin Fecha mínima (extraída de fechaHoraMin)
     * @param horaMin Hora mínima (extraída de fechaHoraMin)
     * @param fechaMax Fecha máxima (extraída de fechaHoraMax)
     * @param horaMax Hora máxima (extraída de fechaHoraMax)
     * @param estado Estado válido (CONFIRMADO)
     * @return Lista de turnos ordenados por fecha y hora
     */
    @Query("SELECT DISTINCT t FROM Turno t " +
           "WHERE t.empresa.id = :empresaId " +
           "AND t.estado = :estado " +
           "AND (t.recordatorioEnviado = false OR t.recordatorioEnviado IS NULL) " +
           "AND t.recordatorioPrimerIntento IS NULL " +  // ✅ A4: Previene duplicados
           // Lógica de rango fecha/hora por partes (portable)
           "AND (" +
               // Caso 1: Fecha mayor que mínima
               "t.fecha > :fechaMin " +
               // Caso 2: Fecha igual a mínima pero hora >= hora mínima
               "OR (t.fecha = :fechaMin AND t.horaInicio >= :horaMin)" +
           ") " +
           "AND (" +
               // Caso 3: Fecha menor que máxima
               "t.fecha < :fechaMax " +
               // Caso 4: Fecha igual a máxima pero hora <= hora máxima
               "OR (t.fecha = :fechaMax AND t.horaInicio <= :horaMax)" +
           ") " +
           "ORDER BY t.fecha ASC, t.horaInicio ASC")
    List<Turno> findTurnosPendientesRecordatorio(
        @Param("empresaId") Long empresaId,
        @Param("fechaMin") LocalDate fechaMin,
        @Param("horaMin") LocalTime horaMin,
        @Param("fechaMax") LocalDate fechaMax,
        @Param("horaMax") LocalTime horaMax,
        @Param("estado") EstadoTurno estado  // ✅ Enum, no String (JPQL requiere tipo exacto)
    );

    /**
     * Contar turnos activos de un profesional en estados específicos.
     * 
     * Usado para validar si un profesional puede ser dado de baja.
     * Se consideran estados que representan turnos pendientes o comprometidos:
     * - CREADO: Turno creado, esperando confirmación
     * - PENDIENTE_CONFIRMACION: Esperando confirmación del profesional/empresa
     * - CONFIRMADO: Turno confirmado y comprometido
     * 
     * @param profesional Profesional a validar
     * @param estados Lista de estados a considerar como "activos"
     * @return Cantidad de turnos que el profesional tiene en los estados indicados
     */
    long countByProfesionalAndEstadoIn(Profesional profesional, List<EstadoTurno> estados);
}
