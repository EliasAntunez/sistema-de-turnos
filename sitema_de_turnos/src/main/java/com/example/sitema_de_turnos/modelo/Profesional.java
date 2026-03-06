package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(length = 500)
    private String descripcion;

    @PrePersist
    private void asignarRol() {
        setRol(RolUsuario.PROFESIONAL);
    }
}
