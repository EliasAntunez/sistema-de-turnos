package com.example.sitema_de_turnos.servicio.notificacion;

import com.example.sitema_de_turnos.dto.notificacion.CancelacionBloqueoData;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Envía emails de cancelación a los clientes afectados por un bloqueo de agenda.
 *
 * El método principal está anotado con @Async para que el profesional reciba
 * la confirmación del bloqueo inmediatamente, mientras los emails se procesan
 * en segundo plano en el executor "notificacionesExecutor".
 *
 * La instancia del template HTML se carga en construcción (igual que
 * EmailNotificationService) para evitar I/O repetido por cada envío.
 */
@Service
public class ServicioNotificacionBloqueo {

    private static final Logger log = LoggerFactory.getLogger(ServicioNotificacionBloqueo.class);
    private static final String TEMPLATE_PATH = "/templates/cancelacion-bloqueo-email.html";

    private final JavaMailSender mailSender;
    private final String htmlTemplate;

    @Value("${app.notification.email.from}")
    private String emailFrom;

    @Value("${app.notification.email.from-name}")
    private String emailFromName;

    @Value("${app.notification.email.enabled:true}")
    private boolean enabled;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public ServicioNotificacionBloqueo(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.htmlTemplate = cargarTemplate();
    }

    /**
     * Notifica a cada cliente afectado por una cancelación masiva de turnos.
     *
     * Ejecutado de forma asíncrona: el hilo del servidor retorna la respuesta HTTP
     * de inmediato; este método corre en "notificacionesExecutor".
     *
     * @param datos Lista de datos para cada email a enviar
     */
    @Async("notificacionesExecutor")
    public void notificarCancelacionPorBloqueo(List<CancelacionBloqueoData> datos) {
        if (!enabled) {
            log.warn("⚠️ Sistema de email deshabilitado — se omiten {} notificaciones de cancelación por bloqueo", datos.size());
            return;
        }
        if (datos.isEmpty()) return;

        log.info("📧 Iniciando envío de {} notificación(es) de cancelación por bloqueo (hilo: {})",
                datos.size(), Thread.currentThread().getName());

        int enviados = 0;
        int errores = 0;
        for (CancelacionBloqueoData dato : datos) {
            try {
                enviarEmail(dato);
                enviados++;
            } catch (Exception e) {
                errores++;
                log.error("❌ Error al notificar cancelación a {} <{}>: {}",
                        dato.getClienteNombre(), dato.getClienteEmail(), e.getMessage());
            }
        }
        log.info("✅ Notificaciones de cancelación: {} enviadas, {} con error", enviados, errores);
    }

    // ==================== PRIVADOS ====================

    private void enviarEmail(CancelacionBloqueoData dato) throws MessagingException, java.io.UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(emailFrom, emailFromName);
        helper.setTo(dato.getClienteEmail());
        helper.setSubject("Tu turno fue cancelado — " + dato.getEmpresaNombre());
        helper.setText(construirCuerpoHTML(dato), true);

        mailSender.send(message);
        log.info("✅ Cancelación notificada a {} <{}>", dato.getClienteNombre(), dato.getClienteEmail());
    }

    private String construirCuerpoHTML(CancelacionBloqueoData dato) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-ES"));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String fechaFormateada = dato.getFecha().format(dateFormatter);
        String diaSemana = dato.getFecha().getDayOfWeek()
                .getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-ES"));
        String diaSemanaCapitalizado = Character.toUpperCase(diaSemana.charAt(0)) + diaSemana.substring(1);

        // Link al formulario de reservas de la empresa
        String reservarUrl = frontendUrl + "/reservar/" + dato.getEmpresaSlug();

        return htmlTemplate
                .replace("{empresaNombre}", dato.getEmpresaNombre())
                .replace("{clienteNombre}", dato.getClienteNombre())
                .replace("{diaSemana}", diaSemanaCapitalizado)
                .replace("{fechaFormateada}", fechaFormateada)
                .replace("{horaInicio}", dato.getHoraInicio().format(timeFormatter))
                .replace("{servicioNombre}", dato.getServicioNombre())
                .replace("{profesionalNombre}", dato.getProfesionalNombre())
                .replace("{reservarUrl}", reservarUrl)
                .replace("{empresaNombreFooter}", dato.getEmpresaNombre());
    }

    private String cargarTemplate() {
        try (InputStream is = getClass().getResourceAsStream(TEMPLATE_PATH)) {
            if (is == null) {
                throw new IllegalStateException("No se encontró el template de email en: " + TEMPLATE_PATH);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error al cargar template de cancelación por bloqueo", e);
        }
    }
}
