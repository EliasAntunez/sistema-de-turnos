package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Horarios de atención de la empresa por día de la semana.
 * Una empresa puede tener múltiples rangos horarios por día (ej: mañana y tarde).
 * Los profesionales solo pueden definir su disponibilidad dentro de estos horarios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "horario_empresa",
    indexes = {
        @Index(name = "idx_horario_empresa_dia",
               columnList = "empresa_id, dia_semana, activo")
    },
    check = {
        @CheckConstraint(name = "chk_horario_hora_valida",
                        constraint = "hora_fin > hora_inicio")
    }
)
public class HorarioEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 10)
    private DiaSemana diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private Boolean activo = true;
}
