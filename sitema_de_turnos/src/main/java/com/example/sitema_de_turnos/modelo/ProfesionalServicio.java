package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla de override para deshabilitar servicios específicos de un profesional.
 * Si no existe registro, el profesional puede ofrecer todos los servicios de sus especialidades.
 * Si existe con activo=false, el profesional NO ofrece ese servicio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profesional_servicio",
       uniqueConstraints = @UniqueConstraint(columnNames = {"profesional_id", "servicio_id"}),
       indexes = {
           @Index(name = "idx_prof_serv_profesional", columnList = "profesional_id"),
           @Index(name = "idx_prof_serv_servicio", columnList = "servicio_id"),
           @Index(name = "idx_prof_serv_activo", columnList = "profesional_id, activo")
       }
)
@Deprecated(since = "1.0", forRemoval = true)
public class ProfesionalServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;

    @ManyToOne
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    /**
     * false = El profesional NO ofrece este servicio (override)
     * true = El profesional SÍ ofrece este servicio (redundante con herencia, pero útil para futuros casos)
     */
    @Column(nullable = false)
    private Boolean activo = false;
}
