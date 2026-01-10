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
@Table(name = "profesionales",
    indexes = {
        @Index(name = "idx_profesional_empresa_activo",
               columnList = "empresa_id")
    }
)
public class Profesional extends Usuario {

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "profesional_especialidad",
        joinColumns = @JoinColumn(name = "profesional_id"),
        inverseJoinColumns = @JoinColumn(name = "especialidad_id"),
        indexes = {
            @Index(name = "idx_prof_esp_profesional", columnList = "profesional_id"),
            @Index(name = "idx_prof_esp_especialidad", columnList = "especialidad_id")
        }
    )
    private Set<Especialidad> especialidades = new HashSet<>();

    /**
     * Servicios que este profesional NO puede ofrecer.
     * Aunque tenga la especialidad requerida, estos servicios están bloqueados.
     * Ejemplo: Un barbero puede no hacer el servicio de "barba".
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "profesional_servicio_bloqueado",
        joinColumns = @JoinColumn(name = "profesional_id"),
        inverseJoinColumns = @JoinColumn(name = "servicio_id"),
        indexes = {
            @Index(name = "idx_prof_serv_bloq_profesional", columnList = "profesional_id"),
            @Index(name = "idx_prof_serv_bloq_servicio", columnList = "servicio_id")
        }
    )
    private Set<Servicio> serviciosBloqueados = new HashSet<>();

    @Column(length = 500)
    private String descripcion;

    @PrePersist
    private void asignarRol() {
        setRol(RolUsuario.PROFESIONAL);
    }
}
