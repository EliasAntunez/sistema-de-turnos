package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "duenos")
public class Dueno extends Usuario {

    @OneToOne(mappedBy = "dueno")
    private Empresa empresa;

    @Column(length = 20)
    private String documento;

    @Column(name = "tipo_documento", length = 10)
    private String tipoDocumento; // DNI, CUIT, etc.

    @PrePersist
    private void asignarRol() {
        setRol(RolUsuario.DUENO);
    }
}
