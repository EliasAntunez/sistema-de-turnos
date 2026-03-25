package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.RegistroDispositivoTokenRequest;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.modelo.DispositivoToken;
import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.repositorio.RepositorioDispositivoToken;
import com.example.sitema_de_turnos.repositorio.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ServicioDispositivoToken {

    private final RepositorioDispositivoToken repositorioDispositivoToken;
    private final RepositorioUsuario repositorioUsuario;

    @Transactional
    public void registrarToken(String emailUsuario, RegistroDispositivoTokenRequest request) {
        Usuario usuario = repositorioUsuario.findByEmail(emailUsuario)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        String tokenNormalizado = request.getToken().trim();

        DispositivoToken existente = repositorioDispositivoToken.findByToken(tokenNormalizado).orElse(null);

        if (existente != null) {
            existente.setUserAgent(request.getUserAgent());
            existente.setUsuario(usuario);
            existente.setFechaActualizacion(LocalDateTime.now());
            repositorioDispositivoToken.save(existente);
            return;
        }

        DispositivoToken nuevo = new DispositivoToken();
        nuevo.setToken(tokenNormalizado);
        nuevo.setUserAgent(request.getUserAgent());
        nuevo.setUsuario(usuario);

        try {
            repositorioDispositivoToken.save(nuevo);
        } catch (DataIntegrityViolationException ex) {
            DispositivoToken recuperado = repositorioDispositivoToken.findByToken(tokenNormalizado)
                .orElseThrow(() -> ex);
            recuperado.setUserAgent(request.getUserAgent());
            recuperado.setUsuario(usuario);
            recuperado.setFechaActualizacion(LocalDateTime.now());
            repositorioDispositivoToken.save(recuperado);
        }
    }
}