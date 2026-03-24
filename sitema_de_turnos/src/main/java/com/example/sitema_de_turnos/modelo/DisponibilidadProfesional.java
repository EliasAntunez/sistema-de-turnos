package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

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

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    private PerfilProfesional profesional;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 10)
    private DiaSemana diaSemana;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "UTC")
    @Column(name = "hora_inicio", nullable = false, columnDefinition = "TIME WITHOUT TIME ZONE")
    private LocalTime horaInicio;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "UTC")
    @Column(name = "hora_fin", nullable = false, columnDefinition = "TIME WITHOUT TIME ZONE")
    private LocalTime horaFin;

    @Column(nullable = false)
    private Boolean activo = true;
}
