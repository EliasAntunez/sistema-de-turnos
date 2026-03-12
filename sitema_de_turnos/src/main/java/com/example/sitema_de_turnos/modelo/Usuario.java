package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios",
    indexes = {
        @Index(name = "idx_usuario_email_lower",
               columnList = "email")
    }
)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String contrasena;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false)
    private Boolean activo = true;

    /**
     * Set de roles del usuario (N-N lógico via @ElementCollection).
     * Un mismo usuario puede tener DUENO y PROFESIONAL simultáneamente.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "usuario_roles",
        joinColumns = @JoinColumn(name = "usuario_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Set<RolUsuario> roles = new HashSet<>();

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        normalizarEmail();
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        normalizarEmail();
        fechaActualizacion = LocalDateTime.now();
    }
    
    /**
     * Normaliza el email a minúsculas y elimina espacios.
     * Previene duplicados por diferencias de mayúsculas/minúsculas.
     */
    private void normalizarEmail() {
        if (email != null) {
            email = email.toLowerCase().trim();
        }
    }
}
