package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "turnos",
    indexes = {
        // Índice compuesto para búsquedas frecuentes (profesional + fecha + estado)
        @Index(name = "idx_turno_profesional_fecha_estado", 
               columnList = "profesional_id, fecha, estado"),
        
        // Índice para reportes por empresa y fecha de creación
        @Index(name = "idx_turno_empresa_fecha_creacion", 
               columnList = "empresa_id, fecha_creacion"),
        
        // Índice optimizado para job batch de recordatorios
        // Query típica: WHERE empresa_id = ? AND fecha = ? AND estado IN ('CONFIRMADO', 'CREADO') ORDER BY hora_inicio
        // Beneficios:
        // - Filtrado rápido por empresa (multi-tenant)
        // - Búsqueda eficiente de turnos de una fecha específica (ej: mañana)
        // - Filtro por estado sin table scan
        // - Sort por hora_inicio ya ordenado en índice (sin sort adicional)
        @Index(name = "idx_turno_recordatorios", 
               columnList = "empresa_id, fecha, estado, hora_inicio")
    },
    check = {
        @CheckConstraint(name = "chk_turno_hora_valida",
                        constraint = "hora_fin > hora_inicio"),
        @CheckConstraint(name = "chk_turno_duracion_positiva",
                        constraint = "duracion_minutos > 0"),
        @CheckConstraint(name = "chk_turno_buffer_positivo",
                        constraint = "buffer_minutos >= 0"),
        @CheckConstraint(name = "chk_turno_precio_positivo",
                        constraint = "precio >= 0")
    }
)
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @ManyToOne
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    /**
     * Duración en minutos del servicio (se guarda del momento de la reserva)
     * Este es el tiempo productivo del servicio, sin incluir el buffer.
     */
    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    /**
     * Buffer en minutos aplicado a este turno específico.
     * Tiempo de preparación/limpieza después del servicio.
     * Se guarda el valor que tenía configurado el servicio al momento de crear el turno.
     * Usado para análisis de tiempo productivo vs no productivo.
     */
    @Column(name = "buffer_minutos", nullable = false)
    private Integer bufferMinutos;

    /**
     * Precio del servicio en el momento de la reserva
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoTurno estado = EstadoTurno.CREADO;

    @Column(name = "motivo_cancelacion", length = 500)
    private String motivoCancelacion;

    /**
     * Quién canceló el turno: CLIENTE, PROFESIONAL, EMPRESA
     */
    @Column(name = "cancelado_por", length = 20)
    private String canceladoPor;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    /**
     * Fecha y hora en que se envió el recordatorio por WhatsApp.
     * Se setea cuando el turno pasa de CREADO a PENDIENTE_CONFIRMACION.
     */
    @Column(name = "fecha_envio_recordatorio")
    private LocalDateTime fechaEnvioRecordatorio;

    /**
     * ID del mensaje de WhatsApp enviado (wamid.XXX).
     * Usado para correlacionar respuestas del webhook.
     */
    @Column(name = "whatsapp_message_id", length = 255)
    private String whatsappMessageId;

    /**
     * Fecha y hora en que el cliente respondió al recordatorio.
     * Se setea cuando llega la respuesta SI/NO vía webhook.
     */
    @Column(name = "fecha_respuesta_recordatorio")
    private LocalDateTime fechaRespuestaRecordatorio;

    @Column(length = 500)
    private String observaciones;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
