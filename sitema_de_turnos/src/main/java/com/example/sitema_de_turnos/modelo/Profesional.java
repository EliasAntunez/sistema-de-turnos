package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profesionales")
public class Profesional extends Usuario {

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "profesional_especialidad",
        joinColumns = @JoinColumn(name = "profesional_id"),
        inverseJoinColumns = @JoinColumn(name = "especialidad_id")
    )
    private Set<Especialidad> especialidades = new HashSet<>();

    @Column(length = 500)
    private String descripcion;

    @PrePersist
    private void asignarRol() {
        setRol(RolUsuario.PROFESIONAL);
    }
}
