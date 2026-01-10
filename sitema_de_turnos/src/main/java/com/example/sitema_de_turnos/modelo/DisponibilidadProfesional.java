package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "disponibilidad_profesional",
       indexes = {
           // Índice para búsquedas por profesional
           @Index(name = "idx_disponibilidad_profesional_activo",
                  columnList = "profesional_id, dia_semana, activo")
       },
       check = {
           @CheckConstraint(name = "chk_disponibilidad_hora_valida",
                           constraint = "hora_fin > hora_inicio")
       }
)
public class DisponibilidadProfesional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;

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
