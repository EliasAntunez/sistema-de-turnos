package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Notificación para un profesional
 * Registra eventos relevantes que el profesional debe conocer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notificaciones",
    indexes = {
        // ✅ B3: Índice compuesto optimizado para queries ordenadas por fecha
        // Usado por: findByProfesionalOrderByFechaCreacionDesc, findTopNByProfesional
        @Index(name = "idx_notif_profesional_fecha", 
               columnList = "profesional_id, fecha_creacion DESC"),
        
        // Índice para buscar notificaciones no leídas de un profesional
        @Index(name = "idx_notif_profesional_leida", 
               columnList = "profesional_id, leida, fecha_creacion"),
        
        // Índice para buscar por tipo
        @Index(name = "idx_notif_tipo", 
               columnList = "tipo, fecha_creacion")
    }
)
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Profesional que recibe la notificación
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    private PerfilProfesional profesional;

    /**
     * Tipo de notificación
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoNotificacion tipo;

    /**
     * Título breve de la notificación
     */
    @Column(nullable = false, length = 200)
    private String titulo;

    /**
     * Mensaje descriptivo de la notificación
     */
    @Column(nullable = false, length = 1000)
    private String mensaje;

    /**
     * Datos adicionales en formato JSON
     * Contiene información detallada del evento (turno, cliente, etc.)
     */
    @Column(name = "contenido_json", columnDefinition = "TEXT")
    private String contenidoJson;

    /**
     * ID del turno relacionado (si aplica)
     */
    @Column(name = "turno_id")
    private Long turnoId;

    /**
     * Indica si la notificación fue leída
     */
    @Column(nullable = false)
    private Boolean leida = false;

    /**
     * Fecha y hora en que se leyó la notificación
     */
    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    /**
     * Fecha y hora de creación de la notificación
     */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }

    /**
     * Marca la notificación como leída
     */
    public void marcarComoLeida() {
        this.leida = true;
        this.fechaLectura = LocalDateTime.now();
    }
}
