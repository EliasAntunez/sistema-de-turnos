package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.ErrorValidacion;
import com.example.sitema_de_turnos.dto.RespuestaApi;
import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;
import com.example.sitema_de_turnos.excepcion.CuentaDesactivadaException;
import com.example.sitema_de_turnos.excepcion.HorarioEnUsoException;
import com.example.sitema_de_turnos.excepcion.OperacionBloqueadaPorTurnosException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.excepcion.SolapamientoException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ManejadorExcepcionesGlobal {

    /**
     * Maneja errores de validación de DTOs (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorValidacion> manejarErroresDeValidacion(MethodArgumentNotValidException ex) {
        
        Map<String, String> errores = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(campo, mensaje);
        });
        
        ErrorValidacion errorValidacion = new ErrorValidacion(
                "Error de validación en los datos enviados",
                errores
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorValidacion);
    }

    /**
     * Maneja recurso no encontrado (404)
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaApi<Void>> manejarRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(RespuestaApi.error(ex.getMessage()));
    }

    /**
     * Maneja acceso denegado a recursos (403)
     */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<RespuestaApi<Void>> manejarAccesoDenegadoCustom(AccesoDenegadoException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(RespuestaApi.error(ex.getMessage()));
    }

    /**
     * Maneja cuentas desactivadas (403)
     */
    @ExceptionHandler(CuentaDesactivadaException.class)
    public ResponseEntity<RespuestaApi<Void>> manejarCuentaDesactivada(CuentaDesactivadaException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(RespuestaApi.error(ex.getMessage()));
    }

    /**
     * Maneja conflictos de negocio genéricos (409)
     */
    @ExceptionHandler(com.example.sitema_de_turnos.excepcion.ConflictoException.class)
    public ResponseEntity<RespuestaApi<Void>> manejarConflicto(com.example.sitema_de_turnos.excepcion.ConflictoException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(RespuestaApi.error(ex.getMessage()));
    }

    /**
     * Maneja solapamiento de turnos (409) — más específico que ConflictoException.
     * Cubre tanto el check en memoria como las race conditions resueltas por el
     * índice único parcial {@code idx_turno_no_overlap} de PostgreSQL.
     * Al ser subclase de ConflictoException, Spring prioriza este handler.
     */
    @ExceptionHandler(SolapamientoException.class)
    public ResponseEntity<RespuestaApi<Void>> manejarSolapamiento(SolapamientoException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(RespuestaApi.error(ex.getMessage()));
    }

    /**
     * Red de seguridad para DataIntegrityViolationException no capturada en la capa de servicio.
     * Ejemplos: unique constraint en otras entidades, FK violations inesperadas.
     * Devuelve 409 en lugar del 400 genérico del catch-all de RuntimeException.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaApi<Void>> manejarViolacionIntegridad(DataIntegrityViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(RespuestaApi.error("El recurso ya existe o viola una regla de unicidad. Por favor, verifica los datos e intenta nuevamente."));
    }

    /**
     * Maneja fallos de concurrencia en BD (incluye serialización y conflictos de escritura).
     * Se traduce como conflicto de disponibilidad para mantener UX consistente en reservas.
     */
    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<RespuestaApi<Void>> manejarFalloConcurrencia(ConcurrencyFailureException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(RespuestaApi.error("El horario seleccionado ya no está disponible. Por favor, selecciona otro horario."));
    }

    /**
     * Maneja el intento de eliminar/modificar un horario que tiene disponibilidades
     * de profesionales activas dentro de ese rango (409).
     * El cuerpo incluye el campo "profesionalesAfectados" con la lista detallada.
     */
    @ExceptionHandler(HorarioEnUsoException.class)
    public ResponseEntity<Map<String, Object>> manejarHorarioEnUso(HorarioEnUsoException ex) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("exito", false);
        cuerpo.put("mensaje", ex.getMessage());
        cuerpo.put("profesionalesAfectados", ex.getProfesionalesAfectados());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(cuerpo);
    }

    /**
     * Maneja el intento de eliminar/modificar un horario que tiene turnos activos
        * (PENDIENTE_CONFIRMACION, PENDIENTE_PAGO o CONFIRMADO) dentro del rango afectado (409).
     * El cuerpo incluye "turnosAfectados" con la descripción de cada turno bloqueante.
     */
    @ExceptionHandler(OperacionBloqueadaPorTurnosException.class)
    public ResponseEntity<Map<String, Object>> manejarOperacionBloqueadaPorTurnos(OperacionBloqueadaPorTurnosException ex) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("exito", false);
        cuerpo.put("mensaje", ex.getMessage());
        cuerpo.put("turnosAfectados", ex.getTurnosAfectados());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(cuerpo);
    }

    /**
     * Maneja errores de acceso denegado de Spring Security (403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespuestaApi<Void>> manejarAccesoDenegado(AccessDeniedException ex) {
        
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(RespuestaApi.error("No tienes permisos para acceder a este recurso"));
    }

    /**
     * Maneja excepciones de negocio genéricas (400)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<RespuestaApi<Void>> manejarExcepcionDeNegocio(RuntimeException ex) {
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(RespuestaApi.error(ex.getMessage()));
    }

    /**
     * Maneja cualquier otra excepción no controlada
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaApi<Void>> manejarExcepcionGeneral(Exception ex) {
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RespuestaApi.error("Error interno del servidor: " + ex.getMessage()));
    }
}
