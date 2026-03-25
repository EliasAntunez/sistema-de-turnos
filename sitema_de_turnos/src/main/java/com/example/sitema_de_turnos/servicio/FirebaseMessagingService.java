package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.modelo.DispositivoToken;
import com.example.sitema_de_turnos.modelo.Usuario;
import com.example.sitema_de_turnos.repositorio.RepositorioDispositivoToken;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseMessagingService {

    private final RepositorioDispositivoToken repositorioDispositivoToken;

    @Async("notificacionesExecutor")
    public void enviarPush(Usuario usuario, String titulo, String cuerpo) {
        try {
            if (usuario == null || usuario.getId() == null) {
                return;
            }

            if (FirebaseApp.getApps().isEmpty()) {
                log.warn("Firebase no inicializado. Se omite envío push para usuario {}", usuario.getEmail());
                return;
            }

            List<DispositivoToken> dispositivos = repositorioDispositivoToken.findByUsuarioId(usuario.getId());
            if (dispositivos.isEmpty()) {
                return;
            }

            List<String> tokens = dispositivos.stream()
                .map(DispositivoToken::getToken)
                .filter(token -> token != null && !token.isBlank())
                .distinct()
                .toList();

            if (tokens.isEmpty()) {
                return;
            }

            String tituloPush = aplicarEmojiPush(titulo);

            final int batchSize = 500;
            int totalSuccess = 0;
            int totalFailure = 0;
            List<String> tokensProcesados = new ArrayList<>();
            List<SendResponse> respuestasProcesadas = new ArrayList<>();

            for (int inicio = 0; inicio < tokens.size(); inicio += batchSize) {
                int fin = Math.min(inicio + batchSize, tokens.size());
                List<String> batchTokens = tokens.subList(inicio, fin);

                MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(batchTokens)
                    .setNotification(Notification.builder()
                        .setTitle(tituloPush)
                        .setBody(cuerpo)
                        .build())
                    .setWebpushConfig(WebpushConfig.builder()
                        .putHeader("Urgency", "high") 
                        .setNotification(WebpushNotification.builder()
                            .setRequireInteraction(true) // Hace que la notificación no desaparezca sola
                            .build())
                        .build())
                    .build();


                BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
                totalSuccess += response.getSuccessCount();
                totalFailure += response.getFailureCount();

                tokensProcesados.addAll(batchTokens);
                respuestasProcesadas.addAll(response.getResponses());
            }

            log.info("Push FCM enviado a usuario {}. success={}, failure={}",
                usuario.getEmail(), totalSuccess, totalFailure);

            limpiarTokensInvalidos(tokensProcesados, respuestasProcesadas);
        } catch (Exception e) {
            log.error("Error en envío asíncrono de push FCM para usuario {}",
                usuario != null ? usuario.getEmail() : "desconocido", e);
        }
    }

    private String aplicarEmojiPush(String titulo) {
        String tituloSeguro = titulo != null ? titulo : "Nueva notificación";
        String tituloTrim = tituloSeguro.trim();

        if (tituloTrim.matches("^[✅❌🔄🔔💰].*")) {
            return tituloSeguro;
        }

        String lower = tituloSeguro.toLowerCase();

        if (lower.contains("nuevo") || lower.contains("reserva") || lower.contains("pago") || lower.contains("seña") || lower.contains("sena")) {
            return "✅ " + tituloSeguro;
        }

        if (lower.contains("cancelado") || lower.contains("rechazado") || lower.contains("inasistencia")) {
            return "❌ " + tituloSeguro;
        }

        if (lower.contains("reprogramado") || lower.contains("cambio")) {
            return "🔄 " + tituloSeguro;
        }

        return "🔔 " + tituloSeguro;
    }

    private void limpiarTokensInvalidos(List<String> tokens, List<SendResponse> responses) {
        List<String> tokensInvalidos = new ArrayList<>();

        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (sendResponse.isSuccessful()) {
                continue;
            }

            FirebaseMessagingException exception = sendResponse.getException();
            if (exception == null || exception.getMessagingErrorCode() == null) {
                continue;
            }

            String code = exception.getMessagingErrorCode().name();
            if ("UNREGISTERED".equals(code) || "INVALID_ARGUMENT".equals(code)) {
                tokensInvalidos.add(tokens.get(i));
            }
        }

        if (!tokensInvalidos.isEmpty()) {
            int eliminados = repositorioDispositivoToken.deleteByTokenIn(tokensInvalidos);
            log.info("Eliminados {} tokens inválidos de FCM", eliminados);
        }
    }
}