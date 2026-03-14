package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.excepcion.CuentaDesactivadaException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.modelo.PerfilDueno;
import com.example.sitema_de_turnos.repositorio.RepositorioPerfilDueno;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio centralizado para validar perfiles de dueño y sus empresas.
 * Composición: busca en repositorioPerfilDueno en lugar de usar instanceof.
 */
@Service
@RequiredArgsConstructor
public class ServicioValidacionDueno {

    private final RepositorioPerfilDueno repositorioPerfilDueno;

    /**
     * Valida y obtiene un PerfilDueno activo con empresa activa.
     *
     * @param email Email del usuario dueño
     * @return PerfilDueno validado
     */
    public PerfilDueno validarYObtenerDueno(String email) {
        PerfilDueno perfil = repositorioPerfilDueno.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado o no tiene perfil de dueño"));

        if (!perfil.getUsuario().getActivo()) {
            throw new CuentaDesactivadaException("Su cuenta ha sido desactivada. Contacte al administrador.");
        }

        if (!perfil.getActivo()) {
            throw new CuentaDesactivadaException("Su perfil de dueño ha sido desactivado. Contacte al administrador.");
        }

        if (perfil.getEmpresa() == null) {
            throw new CuentaDesactivadaException("No tiene una empresa asignada");
        }

        if (!perfil.getEmpresa().getActiva()) {
            throw new CuentaDesactivadaException("Su empresa ha sido desactivada. Contacte al administrador.");
        }

        return perfil;
    }
}
