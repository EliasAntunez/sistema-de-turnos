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
        // Query típica: WHERE empresa_id = ? AND fecha = ? AND estado = 'CONFIRMADO' ORDER BY hora_inicio
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
    private PerfilProfesional profesional;

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
    private EstadoTurno estado;

    @Column(name = "motivo_cancelacion", length = 500)
    private String motivoCancelacion;

    /**
     * Snapshot contractual de horas límite de cancelación vigente
     * al momento de crear la reserva.
     */
    @Column(name = "horas_limite_cancelacion_aplicada")
    private Integer horasLimiteCancelacionAplicada;

    /**
     * Quién canceló el turno: CLIENTE, PROFESIONAL, EMPRESA
     */
    @Column(name = "cancelado_por", length = 20)
    private String canceladoPor;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    /**
     * ID del turno original del cual proviene esta reserva reprogramada.
     * Null cuando el turno fue creado originalmente (sin clonación).
     */
    @Column(name = "turno_origen_id")
    private Long turnoOrigenId;

    @Column(name = "cantidad_reprogramaciones_cliente", nullable = false, columnDefinition = "integer default 0")
    private Integer cantidadReprogramacionesCliente = 0;

    @Column(length = 500)
    private String observaciones;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // ===========================
    // Campos para Sistema de Recordatorios
    // ===========================
    
    /**
     * Indica si el recordatorio fue enviado exitosamente
     */
    @Column(name = "recordatorio_enviado")
    private Boolean recordatorioEnviado = false;

    /**
     * Fecha y hora en que se envió el recordatorio exitosamente
     */
    @Column(name = "fecha_recordatorio_enviado")
    private LocalDateTime fechaRecordatorioEnviado;

    /**
     * Marca temporal del primer intento de envío (A4 - Previene duplicados).
     * Se setea ANTES de iniciar el proceso de envío para que el scheduler
     * no vuelva a seleccionar este turno en la siguiente ejecución.
     * Si el proceso falla completamente, se resetea a NULL para reintentar más tarde.
     */
    @Column(name = "recordatorio_primer_intento")
    private LocalDateTime recordatorioPrimerIntento;

    /**
     * Número de intentos de envío de recordatorio
     */
    @Column(name = "recordatorio_intentos")
    private Integer recordatorioIntentos = 0;

    /**
     * Mensaje de error si el envío falló (últimos 500 caracteres)
     */
    @Column(name = "recordatorio_error", length = 500)
    private String recordatorioError;

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
