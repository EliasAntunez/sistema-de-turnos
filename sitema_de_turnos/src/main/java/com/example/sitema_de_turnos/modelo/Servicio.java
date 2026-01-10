package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
                        constraint = "buffer_minutos IS NULL OR buffer_minutos >= 0")
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
     * Si es null, usa el buffer por defecto de la empresa.
     */
    @Column(name = "buffer_minutos")
    private Integer bufferMinutos;

    /**
     * Precio del servicio
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    /**
     * Especialidades que pueden ofrecer este servicio
     * Ejemplo: Servicio "Corte de cabello" puede estar en especialidades "Barbería" y "Peluquería"
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "servicio_especialidad",
        joinColumns = @JoinColumn(name = "servicio_id"),
        inverseJoinColumns = @JoinColumn(name = "especialidad_id"),
        indexes = {
            @Index(name = "idx_serv_esp_servicio", columnList = "servicio_id"),
            @Index(name = "idx_serv_esp_especialidad", columnList = "especialidad_id")
        }
    )
    private Set<Especialidad> especialidades = new HashSet<>();

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
