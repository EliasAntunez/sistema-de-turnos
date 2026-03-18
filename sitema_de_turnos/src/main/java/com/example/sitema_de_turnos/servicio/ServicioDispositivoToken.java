package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.dto.RegistroDispositivoTokenRequest;
import com.example.sitema_de_turnos.excepcion.RecursoNoEncontradoException;
import com.example.sitema_de_turnos.modelo.DispositivoToken;
import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.repositorio.RepositorioDispositivoToken;
import com.example.sitema_de_turnos.repositorio.RepositorioUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        DispositivoToken dispositivoToken = repositorioDispositivoToken.findByToken(tokenNormalizado)
            .orElseGet(DispositivoToken::new);

        dispositivoToken.setToken(tokenNormalizado);
        dispositivoToken.setUserAgent(request.getUserAgent());
        dispositivoToken.setUsuario(usuario);

        repositorioDispositivoToken.save(dispositivoToken);
    }
}