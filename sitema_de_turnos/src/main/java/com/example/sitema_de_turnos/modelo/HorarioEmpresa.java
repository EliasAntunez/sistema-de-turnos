package com.example.sitema_de_turnos.modelo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_horario_empresa_rango",
            columnNames = {"empresa_id", "dia_semana", "hora_inicio", "hora_fin"}
        )
    },
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

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 10)
    private DiaSemana diaSemana;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Column(name = "hora_inicio", nullable = false, columnDefinition = "TIME WITHOUT TIME ZONE")
    private LocalTime horaInicio;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Column(name = "hora_fin", nullable = false, columnDefinition = "TIME WITHOUT TIME ZONE")
    private LocalTime horaFin;

    @Column(nullable = false)
    private Boolean activo = true;
}
