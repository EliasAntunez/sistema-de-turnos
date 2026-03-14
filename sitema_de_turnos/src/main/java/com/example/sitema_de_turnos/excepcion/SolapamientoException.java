package com.example.sitema_de_turnos.excepcion;

/**
 * Excepción lanzada cuando se detecta solapamiento de turnos.
 *
 * Dos escenarios posibles:
 *
 * 1. CHECK DE APLICACIÓN (normal): {@code existeSolapamiento()} detecta en el SELECT
 *    que ya existe un turno activo en ese slot. → Lanzada directamente en ServicioTurno.
 *
 * 2. RACE CONDITION (concurrencia): dos hilos pasan simultáneamente el check en memoria,
 *    luego intentan el INSERT en la misma fracción de segundo. El primero gana; el segundo
 *    viola el índice único parcial {@code idx_turno_no_overlap} en PostgreSQL y produce
 *    una {@code DataIntegrityViolationException}, que el servicio intercepta y relanza
 *    como esta excepción. → El segundo cliente recibe HTTP 409 con el mensaje correcto.
 *
 * Extiende {@link ConflictoException} para ser mapeada automáticamente a HTTP 409 Conflict
 * por el {@code ManejadorExcepcionesGlobal}.
 */
public class SolapamientoException extends ConflictoException {

    public SolapamientoException(String mensaje) {
        super(mensaje);
    }

    public SolapamientoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
