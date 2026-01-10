package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;
import com.example.sitema_de_turnos.excepcion.CuentaDesactivadaException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.modelo.Profesional;
import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.repositorio.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio centralizado para validar profesionales y sus empresas.
 * Análogo a ServicioValidacionDueno.
 * 
 * CORRECCIÓN DE SEGURIDAD:
 * Antes: ServicioDisponibilidad, ServicioBloqueoFecha validaban solo si usuario era Profesional
 * Ahora: Valida además que profesional esté activo y su empresa esté activa
 */
@Service
@RequiredArgsConstructor
public class ServicioValidacionProfesional {

    private final RepositorioUsuario repositorioUsuario;

    /**
     * Valida y obtiene un profesional activo con empresa activa.
     * 
     * @param email Email del profesional
     * @return Profesional validado
     * @throws RecursoNoEncontradoException si el usuario no existe
     * @throws AccesoDenegadoException si el usuario no es un profesional
     * @throws CuentaDesactivadaException si el profesional o su empresa están desactivados
     */
    public Profesional validarYObtenerProfesional(String email) {
        Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        
        if (!(usuario instanceof Profesional)) {
            throw new AccesoDenegadoException("El usuario no es un profesional");
        }
        
        Profesional profesional = (Profesional) usuario;
        
        if (!profesional.getActivo()) {
            throw new CuentaDesactivadaException("Su cuenta ha sido desactivada. Contacte al administrador.");
        }
        
        if (profesional.getEmpresa() == null) {
            throw new CuentaDesactivadaException("No tiene una empresa asignada");
        }
        
        if (!profesional.getEmpresa().getActiva()) {
            throw new CuentaDesactivadaException("Su empresa ha sido desactivada. Contacte al administrador.");
        }
        
        return profesional;
    }
}
