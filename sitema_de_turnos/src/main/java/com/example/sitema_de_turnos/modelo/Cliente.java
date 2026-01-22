package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clientes", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_cliente_empresa_telefono",
                         columnNames = {"empresa_id", "telefono"})
    },
    indexes = {
        @Index(name = "idx_cliente_empresa_activo",
               columnList = "empresa_id, activo")
    },
    check = {
        @CheckConstraint(name = "chk_telefono_formato",
                        constraint = "telefono ~ '^[\\+]?[0-9\\s\\-\\(\\)]{7,20}$'")
    }
)
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(length = 150)
    private String email;

    @JsonIgnore
    @Column(length = 255)
    private String contrasena;

    /**
     * Indica si el cliente tiene cuenta de usuario (con contraseña)
     * false = invitado (solo reservó sin crear cuenta)
     * true = tiene cuenta y puede hacer login
     */
    @Column(name = "tiene_usuario", nullable = false)
    private Boolean tieneUsuario = false;

    /**
     * Indica si el teléfono fue validado (SMS/WhatsApp)
     */
    @Column(name = "telefono_validado", nullable = false)
    private Boolean telefonoValidado = false;

    @Column(length = 20)
    private String documento;

    @Column(name = "tipo_documento", length = 10)
    private String tipoDocumento;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 500)
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
