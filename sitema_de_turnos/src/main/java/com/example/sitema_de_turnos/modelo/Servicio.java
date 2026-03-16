package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "servicios",
    indexes = {
        @Index(name = "idx_servicio_empresa_activo",
               columnList = "empresa_id, activo")
    },
    check = {
        @CheckConstraint(name = "chk_servicio_duracion_positiva",
                        constraint = "duracion_minutos > 0"),
        @CheckConstraint(name = "chk_servicio_precio_positivo",
                        constraint = "precio >= 0"),
        @CheckConstraint(name = "chk_servicio_buffer_positivo",
                        constraint = "buffer_minutos >= 0"),
        @CheckConstraint(name = "chk_servicio_monto_sena_positivo",
                        constraint = "monto_sena IS NULL OR monto_sena >= 0")
    }
)
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    /**
     * Duración estimada del servicio en minutos
     */
    @Column(nullable = false)
    private Integer duracionMinutos;

    /**
     * Buffer en minutos después de finalizar este servicio.
     * Tiempo de espera antes del próximo turno (limpieza, preparación, descanso, etc.)
     * Nunca es null: al crear se resuelve contra el buffer por defecto de la empresa.
     */
    @Column(name = "buffer_minutos", nullable = false)
    private Integer bufferMinutos;

    /**
     * Precio del servicio
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "requiere_sena", nullable = false, columnDefinition = "boolean default false")
    private Boolean requiereSena = false;

    @Column(name = "monto_sena", precision = 10, scale = 2)
    private BigDecimal montoSena;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (requiereSena == null) {
            requiereSena = false;
        }
        fechaCreacion = LocalDateTime.now();
    }
}
