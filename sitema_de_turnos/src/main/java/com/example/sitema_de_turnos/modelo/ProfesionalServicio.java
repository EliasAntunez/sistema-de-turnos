package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla para habilitar servicios específicos de un profesional.
 * Si existe registro con activo=true, el profesional puede ofrecer ese servicio.
 * Si no existe registro, el profesional NO ofrece ese servicio.
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
     * true = El profesional ofrece este servicio
     * false = El profesional NO ofrece este servicio (deshabilitado)
     */
    @Column(nullable = false)
    private Boolean activo = false;
}
