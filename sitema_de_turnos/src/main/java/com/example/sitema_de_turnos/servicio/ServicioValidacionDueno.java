package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.excepcion.AccesoDenegadoException;
import com.example.sitema_de_turnos.excepcion.CuentaDesactivadaException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.modelo.Dueno;
import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.repositorio.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio centralizado para validar dueños y sus empresas.
 * Evita duplicación de lógica entre diferentes servicios.
 */
@Service
@RequiredArgsConstructor
public class ServicioValidacionDueno {

    private final RepositorioUsuario repositorioUsuario;

    /**
     * Valida y obtiene un dueño activo con empresa activa.
     * 
     * @param email Email del dueño
     * @return Dueño validado
     * @throws RecursoNoEncontradoException si el usuario no existe
     * @throws AccesoDenegadoException si el usuario no es un dueño
     * @throws CuentaDesactivadaException si el dueño o su empresa están desactivados
     */
    public Dueno validarYObtenerDueno(String email) {
        Usuario usuario = repositorioUsuario.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        
        if (!(usuario instanceof Dueno)) {
            throw new AccesoDenegadoException("El usuario no es un dueño de empresa");
        }
        
        Dueno dueno = (Dueno) usuario;
        
        if (!dueno.getActivo()) {
            throw new CuentaDesactivadaException("Su cuenta ha sido desactivada. Contacte al administrador.");
        }
        
        if (dueno.getEmpresa() == null) {
            throw new CuentaDesactivadaException("No tiene una empresa asignada");
        }
        
        if (!dueno.getEmpresa().getActiva()) {
            throw new CuentaDesactivadaException("Su empresa ha sido desactivada. Contacte al administrador.");
        }
        
        return dueno;
    }
}
