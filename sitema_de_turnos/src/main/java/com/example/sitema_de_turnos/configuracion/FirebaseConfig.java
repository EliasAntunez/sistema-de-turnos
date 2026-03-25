package com.example.sitema_de_turnos.configuracion;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${FIREBASE_CONFIG_JSON:}")
    private String firebaseConfigJson;

    @Value("${firebase.service-account.path:firebase-service-account.json}")
    private String serviceAccountPath;

    @PostConstruct
    public void inicializarFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try {
            if (firebaseConfigJson != null && !firebaseConfigJson.isBlank()) {
                String normalizedJson = firebaseConfigJson.replace("\\n", "\n").trim();

                try (InputStream serviceAccount = new ByteArrayInputStream(normalizedJson.getBytes(StandardCharsets.UTF_8))) {
                    FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                    FirebaseApp.initializeApp(options);
                    log.info("Firebase Admin SDK inicializado desde FIREBASE_CONFIG_JSON");
                    return;
                }
            }

            ClassPathResource serviceAccountResource = new ClassPathResource(serviceAccountPath);
            if (!serviceAccountResource.exists()) {
                log.warn("No se encontró FIREBASE_CONFIG_JSON ni {} en classpath. Push FCM quedará deshabilitado.", serviceAccountPath);
                return;
            }

            try (InputStream serviceAccount = serviceAccountResource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK inicializado correctamente");
            }
        } catch (Exception e) {
            log.error("Error al inicializar Firebase Admin SDK. Push FCM deshabilitado.", e);
        }
    }
}