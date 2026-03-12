package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Perfil de Profesional/Empleado.
 * Composición sobre herencia: un Usuario puede tener este perfil
 * y simultáneamente también un PerfilDueno en la misma empresa (dueño-profesional).
 * Un mismo usuario podría ser profesional en múltiples empresas (lista).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "perfiles_profesional",
    indexes = {
        @Index(name = "idx_perfil_prof_empresa_activo",
               columnList = "empresa_id, activo"),
        // Necesario para findByUsuarioEmail: JOIN usuarios u ON u.id = pp.usuario_id WHERE u.email = ?
        @Index(name = "idx_perfil_prof_usuario",
               columnList = "usuario_id")
    }
)
public class PerfilProfesional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(length = 500)
    private String descripcion;

    /** Permite desactivar solo el perfil profesional sin afectar el acceso del usuario. */
    @Column(nullable = false)
    private Boolean activo = true;
}
