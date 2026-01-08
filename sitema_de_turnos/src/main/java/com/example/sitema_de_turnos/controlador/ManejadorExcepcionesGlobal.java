package com.example.sitema_de_turnos.controlador;

import com.example.sitema_de_turnos.dto.ErrorValidacion;
import com.example.sitema_de_turnos.dto.RespuestaApi;
import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;
import com.example.sitema_de_turnos.excepcion.CuentaDesactivadaException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
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
