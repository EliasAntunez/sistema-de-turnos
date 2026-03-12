package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.excepcion.CuentaDesactivadaException;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.modelo.PerfilProfesional;
import com.example.sitema_de_turnos.repositorio.RepositorioPerfilProfesional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio centralizado para validar perfiles de profesional y sus empresas.
 * Composición: busca en repositorioPerfilProfesional en lugar de usar instanceof.
 */
@Service
@RequiredArgsConstructor
public class ServicioValidacionProfesional {

    private final RepositorioPerfilProfesional repositorioPerfilProfesional;

    /**
     * Valida y obtiene un PerfilProfesional activo con empresa activa.
     *
     * @param email Email del usuario profesional
     * @return PerfilProfesional validado
     */
    public PerfilProfesional validarYObtenerProfesional(String email) {
        PerfilProfesional perfil = repositorioPerfilProfesional.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado o no tiene perfil de profesional"));

        if (!perfil.getUsuario().getActivo()) {
            throw new CuentaDesactivadaException("Su cuenta ha sido desactivada. Contacte al administrador.");
        }

        if (!perfil.getActivo()) {
            throw new CuentaDesactivadaException("Su perfil de profesional ha sido desactivado. Contacte al administrador.");
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
