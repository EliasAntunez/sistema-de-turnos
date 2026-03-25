package com.example.sitema_de_turnos.servicio.notificacion;

import com.example.sitema_de_turnos.dto.notificacion.ReminderData;
import com.example.sitema_de_turnos.excepcion.NotificationException;
import com.example.sitema_de_turnos.modelo.Pago;
import com.example.sitema_de_turnos.modelo.Turno;
import com.example.sitema_de_turnos.repositorio.RepositorioPago;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementación de NotificationService usando API HTTP de Brevo
 * ✅ B6: Template HTML externalizado en resources/templates/
 */
@Service
public class EmailNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final String BREVO_SENDER_EMAIL = "ansa.soft.2026@gmail.com";
    private static final String BREVO_SENDER_NAME = "Sistema de Turnos";
    private static final String TEMPLATE_RECORDATORIO_PATH = "/templates/recordatorio-email.html";
    private static final String TEMPLATE_CONFIRMACION_PATH = "/templates/confirmacion-turno-email.html";
    private static final String TEMPLATE_REPROGRAMACION_PROFESIONAL = "reprogramacion-por-profesional";
    private static final String TEMPLATE_REPROGRAMACION_CLIENTE = "reprogramacion-por-cliente";
    private static final String TEMPLATE_TURNO_EXPIRADO = "turno-expirado";
    private static final DateTimeFormatter FORMATTER_FECHA_TURNO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATTER_HORA_TURNO = DateTimeFormatter.ofPattern("HH:mm");
    
    private final RestTemplate restTemplate;
    private final RepositorioPago repositorioPago;
    private final TemplateEngine templateEngine;
    private final String reminderTemplate;
    private final String confirmacionTurnoTemplate;
    
    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;
    
    @Value("${app.notification.email.enabled:true}")
    private boolean enabled;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;
    
    public EmailNotificationService(RepositorioPago repositorioPago, TemplateEngine templateEngine) {
        this.restTemplate = new RestTemplate();
        this.repositorioPago = repositorioPago;
        this.templateEngine = templateEngine;
        this.reminderTemplate = cargarTemplate(TEMPLATE_RECORDATORIO_PATH);
        this.confirmacionTurnoTemplate = cargarTemplate(TEMPLATE_CONFIRMACION_PATH);
    }
    
    @Override
    public void sendReminder(ReminderData data) {
        if (!enabled) {
            log.warn("⚠️ Sistema de email deshabilitado - No se enviará recordatorio para turno {}", data.getTurnoId());
            return;
        }
        try {
            validateReminderData(data);
            String subject = "⏰ Recordatorio de turno - " + data.getEmpresaNombre();
            String html = construirCuerpoHTML(data);
            enviarEmailBrevo(data.getClienteEmail(), data.getClienteNombre(), subject, html);
            
            log.info("✅ Recordatorio enviado exitosamente - Turno {} - Email: {}", 
                    data.getTurnoId(), data.getClienteEmail());
        } catch (Exception e) {
            log.error("❌ Error inesperado al enviar recordatorio para turno {}: {}",
                    data != null ? data.getTurnoId() : null,
                    e.getMessage(),
                    e);
        }
    }

    @Async("notificacionesExecutor")
    public void enviarCorreoConfirmacionTurno(Turno turno) {
        String emailDestino = turno != null && turno.getCliente() != null ? turno.getCliente().getEmail() : "desconocido";
        try {
            if (!enabled) {
                log.warn("⚠️ Sistema de email deshabilitado - No se enviará confirmación para turno {}",
                        turno != null ? turno.getId() : null);
                return;
            }

            validarDatosTurno(turno);

            BigDecimal precioTotal = valorSeguro(turno.getPrecio());
            BigDecimal montoSena = repositorioPago.findByTurnoId(turno.getId())
                    .map(Pago::getMonto)
                    .map(this::valorSeguro)
                    .orElse(BigDecimal.ZERO);
            BigDecimal saldoRestante = precioTotal.subtract(montoSena).max(BigDecimal.ZERO);

                String subject = "✅ Confirmación de turno - " + turno.getEmpresa().getNombre();
                String html = construirCuerpoConfirmacionTurno(turno, precioTotal, montoSena, saldoRestante);
                enviarEmailBrevo(turno.getCliente().getEmail(), turno.getCliente().getNombre(), subject, html);

            log.info("✅ Confirmación de turno enviada - Turno {} - Email: {}",
                    turno.getId(), turno.getCliente().getEmail());
        } catch (Exception e) {
            log.error("Error al enviar el correo a {}: ", emailDestino, e);
        }
    }

    @Async("notificacionesExecutor")
    public void enviarCorreoReprogramacionPorProfesional(Turno turno) {
        enviarCorreoReprogramacionTurno(turno, TEMPLATE_REPROGRAMACION_PROFESIONAL,
            "📅 Tu turno fue reprogramado - ");
    }

    @Async("notificacionesExecutor")
    public void enviarCorreoReprogramacionPorCliente(Turno turno) {
        enviarCorreoReprogramacionTurno(turno, TEMPLATE_REPROGRAMACION_CLIENTE,
            "✅ Reprogramación confirmada - ");
    }

    @Async("notificacionesExecutor")
    public void enviarCorreoTurnoExpirado(Turno turno) {
        String emailDestino = turno != null && turno.getCliente() != null ? turno.getCliente().getEmail() : "desconocido";
        try {
            if (!enabled) {
                log.warn("⚠️ Sistema de email deshabilitado - No se enviará expiración para turno {}",
                        turno != null ? turno.getId() : null);
                return;
            }

            validarDatosTurno(turno);

            String subject = "⚠️ Tu turno fue cancelado por falta de pago - " + turno.getEmpresa().getNombre();

            String fecha = turno.getFecha().format(FORMATTER_FECHA_TURNO);
            String horaInicio = turno.getHoraInicio().format(FORMATTER_HORA_TURNO);
            String horaFin = turno.getHoraInicio().plusMinutes(turno.getDuracionMinutos()).format(FORMATTER_HORA_TURNO);
            String slugEmpresa = turno.getEmpresa() != null ? turno.getEmpresa().getSlug() : null;
            String frontendBase = frontendUrl != null ? frontendUrl.replaceAll("/+$", "") : "";
            String urlReserva = frontendBase + "/empresa/" + slugEmpresa;

            Context context = new Context(new Locale("es", "AR"));
            context.setVariable("clienteNombre", turno.getCliente().getNombre());
            context.setVariable("empresaNombre", turno.getEmpresa().getNombre());
            context.setVariable("servicioNombre", turno.getServicio().getNombre());
            context.setVariable("fecha", fecha);
            context.setVariable("horaInicio", horaInicio);
            context.setVariable("horaFin", horaFin);
            context.setVariable("urlReserva", urlReserva);

            String html = templateEngine.process(TEMPLATE_TURNO_EXPIRADO, context);
            enviarEmailBrevo(turno.getCliente().getEmail(), turno.getCliente().getNombre(), subject, html);

            log.info("✅ Correo de expiración de turno enviado - Turno {} - Email: {}",
                    turno.getId(), turno.getCliente().getEmail());
        } catch (Exception e) {
            log.error("Error al enviar correo de expiración a {}: ", emailDestino, e);
        }
    }

    private void enviarCorreoReprogramacionTurno(Turno turno, String templateNombre, String prefijoAsunto) {
        String emailDestino = turno != null && turno.getCliente() != null ? turno.getCliente().getEmail() : "desconocido";
        try {
            if (!enabled) {
                log.warn("⚠️ Sistema de email deshabilitado - No se enviará reprogramación para turno {}",
                        turno != null ? turno.getId() : null);
                return;
            }

            validarDatosTurno(turno);

            String subject = prefijoAsunto + turno.getEmpresa().getNombre();

            String fecha = turno.getFecha().format(FORMATTER_FECHA_TURNO);
            String horaInicio = turno.getHoraInicio().format(FORMATTER_HORA_TURNO);
            String horaFin = turno.getHoraInicio().plusMinutes(turno.getDuracionMinutos()).format(FORMATTER_HORA_TURNO);
            String profesional = turno.getProfesional().getUsuario().getNombre() + " "
                    + turno.getProfesional().getUsuario().getApellido();
                String slugEmpresa = turno.getEmpresa() != null ? turno.getEmpresa().getSlug() : null;
                String frontendBase = frontendUrl != null ? frontendUrl.replaceAll("/+$", "") : "";
                String urlLogin = frontendBase + "/empresa/" + slugEmpresa + "/login-cliente";

            Context context = new Context(new Locale("es", "AR"));
            context.setVariable("clienteNombre", turno.getCliente().getNombre());
            context.setVariable("profesionalNombre", profesional.trim());
            context.setVariable("empresaNombre", turno.getEmpresa().getNombre());
            context.setVariable("servicioNombre", turno.getServicio().getNombre());
            context.setVariable("fecha", fecha);
            context.setVariable("horaInicio", horaInicio);
            context.setVariable("horaFin", horaFin);
            context.setVariable("urlLogin", urlLogin);

            String html = templateEngine.process(templateNombre, context);

            enviarEmailBrevo(turno.getCliente().getEmail(), turno.getCliente().getNombre(), subject, html);

            log.info("✅ Reprogramación de turno enviada - Turno {} - Email: {}",
                    turno.getId(), turno.getCliente().getEmail());
        } catch (Exception e) {
            log.error("Error al enviar correo de reprogramación a {}: ", emailDestino, e);
        }
    }

    private void enviarEmailBrevo(String emailDestino, String nombreDestino, String subject, String htmlContent) {
        try {
            if (brevoApiKey == null || brevoApiKey.isBlank()) {
                log.error("BREVO_API_KEY no está configurada. No se enviará email a {}", emailDestino);
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> sender = new HashMap<>();
            sender.put("name", BREVO_SENDER_NAME);
            sender.put("email", BREVO_SENDER_EMAIL);

            Map<String, String> destinatario = new HashMap<>();
            destinatario.put("email", emailDestino);
            if (nombreDestino != null && !nombreDestino.isBlank()) {
                destinatario.put("name", nombreDestino);
            }

            List<Map<String, String>> to = new ArrayList<>();
            to.add(destinatario);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", sender);
            body.put("to", to);
            body.put("subject", subject);
            body.put("htmlContent", htmlContent);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_API_URL, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Brevo devolvió estado no exitoso al enviar email a {}: {} - {}",
                        emailDestino,
                        response.getStatusCode(),
                        response.getBody());
            }
        } catch (Exception e) {
            log.error("Error al enviar email vía Brevo a {}: {}", emailDestino, e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isAvailable() {
        return enabled && brevoApiKey != null && !brevoApiKey.isBlank();
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
    private String cargarTemplate(String templatePath) {
        try (InputStream is = getClass().getResourceAsStream(templatePath)) {
            if (is == null) {
                throw new NotificationException("No se encontró template HTML en: " + templatePath);
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
        return reminderTemplate
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

    private String construirCuerpoConfirmacionTurno(
            Turno turno,
            BigDecimal precioTotal,
            BigDecimal montoSena,
            BigDecimal saldoRestante
    ) {
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

        String fecha = turno.getFecha().format(FORMATTER_FECHA_TURNO);
        String hora = turno.getHoraInicio().format(FORMATTER_HORA_TURNO);
        String profesional = turno.getProfesional().getUsuario().getNombre() + " "
                + turno.getProfesional().getUsuario().getApellido();

        return confirmacionTurnoTemplate
                .replace("{clienteNombre}", escaparHtml(turno.getCliente().getNombre()))
                .replace("{fecha}", escaparHtml(fecha))
                .replace("{hora}", escaparHtml(hora))
                .replace("{profesionalNombre}", escaparHtml(profesional.trim()))
                .replace("{empresaNombre}", escaparHtml(turno.getEmpresa().getNombre()))
                .replace("{servicioNombre}", escaparHtml(turno.getServicio().getNombre()))
                .replace("{precioTotal}", escaparHtml(currency.format(precioTotal)))
                .replace("{montoSena}", escaparHtml(currency.format(montoSena)))
                .replace("{saldoRestante}", escaparHtml(currency.format(saldoRestante)));
    }

    private void validarDatosTurno(Turno turno) {
        if (turno == null) {
            throw new NotificationException("Turno no puede ser null para enviar confirmación");
        }
        if (turno.getCliente() == null || turno.getCliente().getEmail() == null || turno.getCliente().getEmail().isBlank()) {
            throw new NotificationException("Email del cliente requerido para confirmación de turno");
        }
        if (turno.getEmpresa() == null || turno.getEmpresa().getNombre() == null) {
            throw new NotificationException("Datos de empresa incompletos para enviar confirmación de turno");
        }
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private String escaparHtml(String valor) {
        if (valor == null) {
            return "";
        }
        return valor
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}
