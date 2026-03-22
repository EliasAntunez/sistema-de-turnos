package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Perfil de Dueño de Empresa.
 * Composición sobre herencia: un Usuario puede tener este perfil
 * (y simultáneamente también un PerfilProfesional en la misma empresa).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "perfiles_dueno")
public class PerfilDueno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(length = 20)
    private String documento;

    @Column(name = "tipo_documento", length = 10)
    private String tipoDocumento;

    @Column(nullable = false)
    private Boolean activo = true;

    /**
     * Relación inversa: la Empresa apunta a este perfil.
     * Un PerfilDueno solo puede tener una Empresa.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(mappedBy = "perfilDueno", fetch = FetchType.LAZY)
    private Empresa empresa;
}
