package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Bloqueos de fechas específicas para un profesional.
 * Permite definir días o rangos de días en los que el profesional no estará disponible
 * (vacaciones, feriados, ausencias, etc.)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bloqueo_fecha",
    indexes = {
        @Index(name = "idx_bloqueo_profesional_rango",
               columnList = "profesional_id, fecha_inicio, fecha_fin, activo")
    },
    check = {
        @CheckConstraint(name = "chk_bloqueo_rango_valido",
                        constraint = "fecha_fin IS NULL OR fecha_fin >= fecha_inicio")
    }
)
public class BloqueoFecha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;

    /**
     * Fecha de inicio del bloqueo (inclusive)
     */
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    /**
     * Fecha de fin del bloqueo (inclusive).
     * Si es null, el bloqueo es solo para la fecha_inicio.
     */
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    /**
     * Motivo del bloqueo (opcional)
     */
    @Column(length = 200)
    private String motivo;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
