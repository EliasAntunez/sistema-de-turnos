package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "politica_cancelacion",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_politica_empresa_tipo_activa", columnNames = {"empresa_id", "tipo", "activa"})
    },
    indexes = {
        @Index(name = "idx_politica_empresa_activa", columnList = "empresa_id, activa")
    }
)
public class PoliticaCancelacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    /**
     * Tipo de política: CANCELACION, INASISTENCIA, AMBOS
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPoliticaCancelacion tipo;

    /**
     * Descripción en texto largo (puede ser markdown o HTML)
     */
    @Column(nullable = false, length = 2000)
    private String descripcion;


    /**
     * Límite de horas antes del turno para cancelar sin penalización
     */
    @Column(name = "horas_limite_cancelacion", nullable = false)
    private Integer horasLimiteCancelacion;

    /**
     * Mensaje corto personalizado para el cliente (recordatorio, WhatsApp, etc)
     */
    @Column(name = "mensaje_cliente", length = 300)
    private String mensajeCliente;

    /**
     * Penalización: NINGUNA, ADVERTENCIA, BLOQUEO, MULTA
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PenalizacionPolitica penalizacion;

    @Column(nullable = false)
    private Boolean activa = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
}
