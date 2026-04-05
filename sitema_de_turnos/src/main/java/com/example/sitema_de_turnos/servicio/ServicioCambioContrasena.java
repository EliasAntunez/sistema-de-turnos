package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.CambiarContrasenaRequest;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.repositorio.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioCambioContrasena {

    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder passwordEncoder;
    private final SessionRegistry sessionRegistry;

    @Transactional
    public void cambiarContrasena(String emailAutenticado, CambiarContrasenaRequest request) {
        Usuario usuario = repositorioUsuario.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getContrasenaActual(), usuario.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual no es correcta");
        }

        if (!request.getNuevaContrasena().equals(request.getConfirmarNuevaContrasena())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La confirmación de la nueva contraseña no coincide");
        }

        if (passwordEncoder.matches(request.getNuevaContrasena(), usuario.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contraseña debe ser distinta a la actual");
        }

        usuario.setContrasena(passwordEncoder.encode(request.getNuevaContrasena()));
        repositorioUsuario.save(usuario);

        invalidarTodasLasSesionesDel(emailAutenticado);
    }

    private void invalidarTodasLasSesionesDel(String email) {
        List<Object> principals = sessionRegistry.getAllPrincipals();

        for (Object principal : principals) {
            if (principal instanceof org.springframework.security.core.userdetails.User userDetails
                    && userDetails.getUsername().equals(email)) {
                List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                for (SessionInformation session : sessions) {
                    session.expireNow();
                }
            }
        }
    }
}
