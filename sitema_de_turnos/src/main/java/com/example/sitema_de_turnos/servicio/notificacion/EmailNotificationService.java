package com.example.sitema_de_turnos.servicio.notificacion;

import com.example.sitema_de_turnos.dto.notificacion.ReminderData;
import com.example.sitema_de_turnos.excepcion.NotificationException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Implementación de NotificationService usando Gmail SMTP
 * ✅ B6: Template HTML externalizado en resources/templates/
 */
@Service
public class EmailNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final String TEMPLATE_PATH = "/templates/recordatorio-email.html";
    
    private final JavaMailSender mailSender;
    private final String htmlTemplate;  // Template cargado en construcción
    
    @Value("${app.notification.email.from}")
    private String emailFrom;
    
    @Value("${app.notification.email.from-name}")
    private String emailFromName;
    
    @Value("${app.notification.email.enabled:true}")
    private boolean enabled;
    
    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.htmlTemplate = cargarTemplate();
    }
    
    @Override
    public void sendReminder(ReminderData data) {
        if (!enabled) {
            log.warn("⚠️ Sistema de email deshabilitado - No se enviará recordatorio para turno {}", data.getTurnoId());
            return;
        }
        
        validateReminderData(data);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(emailFrom, emailFromName);
            helper.setTo(data.getClienteEmail());
            helper.setSubject("⏰ Recordatorio de turno - " + data.getEmpresaNombre());
            helper.setText(construirCuerpoHTML(data), true);
            
            mailSender.send(message);
            
            log.info("✅ Recordatorio enviado exitosamente - Turno {} - Email: {}", 
                    data.getTurnoId(), data.getClienteEmail());
            
        } catch (MessagingException e) {
            String errorMsg = "Error al construir mensaje de email para turno " + data.getTurnoId();
            log.error("❌ {}: {}", errorMsg, e.getMessage());
            throw new NotificationException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Error inesperado al enviar email para turno " + data.getTurnoId();
            log.error("❌ {}: {}", errorMsg, e.getMessage(), e);
            throw new NotificationException(errorMsg, e);
        }
    }
    
    @Override
    public boolean isAvailable() {
        return enabled && mailSender != null;
    }
    
    @Override
    public String getNotificationType() {
        return "EMAIL";
    }
    
    private void validateReminderData(ReminderData data) {
        if (data == null) {
            throw new NotificationException("ReminderData no puede ser null");
        }
        if (data.getClienteEmail() == null || data.getClienteEmail().isBlank()) {
            throw new NotificationException("Email del cliente requerido");
        }
        if (data.getFecha() == null) {
            throw new NotificationException("Fecha del turno requerida");
        }
        if (data.getHoraInicio() == null) {
            throw new NotificationException("Hora de inicio requerida");
        }
    }
    
    /**
     * ✅ B6: Carga el template HTML desde resources al iniciar el servicio
     * Evita tener 150+ líneas de HTML hardcodeadas en Java
     */
    private String cargarTemplate() {
        try (InputStream is = getClass().getResourceAsStream(TEMPLATE_PATH)) {
            if (is == null) {
                throw new NotificationException("No se encontró template HTML en: " + TEMPLATE_PATH);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new NotificationException("Error al cargar template HTML", e);
        }
    }
    
    /**
     * ✅ B6: Construye HTML desde template externalizado
     * Los placeholders {variable} se reemplazan con los datos del turno
     */
    private String construirCuerpoHTML(ReminderData data) {
        // Formatear fecha (sin día de la semana, se maneja por separado)
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-ES"));
        String fechaFormateada = data.getFecha().format(dateFormatter);
        String diaSemana = data.getFecha().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-ES"));
        
        // Formatear hora (SOLO INICIO)
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String horaInicioStr = data.getHoraInicio().format(timeFormatter);
        
        // Calcular horas restantes hasta el turno
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaHoraTurno = data.getFechaHoraCompleta();
        long horasRestantes = Duration.between(ahora, fechaHoraTurno).toHours();
        
        // Construir texto de tiempo restante
        String tiempoRestante;
        if (horasRestantes < 1) {
            tiempoRestante = "en menos de 1 hora";
        } else if (horasRestantes == 1) {
            tiempoRestante = "en 1 hora";
        } else if (horasRestantes < 24) {
            tiempoRestante = "en " + horasRestantes + " horas";
        } else {
            long dias = horasRestantes / 24;
            tiempoRestante = dias == 1 ? "mañana" : "en " + dias + " días";
        }
        
        // Preparar valores con fallbacks seguros
        String clienteNombre = data.getClienteNombre() != null ? data.getClienteNombre() : "Cliente";
        String diaSemanaCapitalizado = diaSemana.substring(0, 1).toUpperCase() + diaSemana.substring(1);
        String fechaCapitalizada = fechaFormateada.substring(0, 1).toUpperCase() + fechaFormateada.substring(1);
        String servicioNombre = data.getServicioNombre() != null ? data.getServicioNombre() : "Servicio";
        String profesionalNombre = data.getProfesionalNombre() != null ? data.getProfesionalNombre() : "Profesional";
        String empresaNombre = data.getEmpresaNombre() != null ? data.getEmpresaNombre() : "Sistema de Turnos";
        
        // Construir ubicación
        StringBuilder ubicacion = new StringBuilder();
        if (data.getEmpresaDireccion() != null && !data.getEmpresaDireccion().isBlank()) {
            ubicacion.append(data.getEmpresaDireccion());
        }
        if (data.getEmpresaCiudad() != null && !data.getEmpresaCiudad().isBlank()) {
            if (ubicacion.length() > 0) ubicacion.append(", ");
            ubicacion.append(data.getEmpresaCiudad());
        }
        if (data.getEmpresaProvincia() != null && !data.getEmpresaProvincia().isBlank()) {
            if (ubicacion.length() > 0) ubicacion.append(", ");
            ubicacion.append(data.getEmpresaProvincia());
        }
        
        // ✅ M5: HTML externalizado en template - solo definir display y valor
        String displayUbicacion = ubicacion.length() > 0 ? "" : "display: none;";
        String ubicacionValor = ubicacion.length() > 0 ? ubicacion.toString() : "";
        
        // ✅ M5: HTML externalizado en template - solo definir display y valores
        String displayTelefono = (data.getEmpresaTelefono() != null && !data.getEmpresaTelefono().isBlank()) ? "" : "display: none;";
        String telefonoHref = data.getEmpresaTelefono() != null ? data.getEmpresaTelefono() : "";
        String telefonoValor = data.getEmpresaTelefono() != null ? data.getEmpresaTelefono() : "";
        
        // Reemplazar placeholders en el template
        return htmlTemplate
            .replace("{empresaNombre}", empresaNombre)
            .replace("{clienteNombre}", clienteNombre)
            .replace("{tiempoRestante}", tiempoRestante)
            .replace("{diaSemana}", diaSemanaCapitalizado)
            .replace("{fechaFormateada}", fechaCapitalizada)
            .replace("{horaInicio}", horaInicioStr)
            .replace("{servicioNombre}", servicioNombre)
            .replace("{profesionalNombre}", profesionalNombre)
            .replace("{displayUbicacion}", displayUbicacion)
            .replace("{ubicacionValor}", ubicacionValor)
            .replace("{empresaNombreFooter}", empresaNombre)
            .replace("{displayTelefono}", displayTelefono)
            .replace("{telefonoHref}", telefonoHref)
            .replace("{telefonoValor}", telefonoValor);
    }
}
