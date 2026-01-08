package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "super_admins")
public class SuperAdmin extends Usuario {

    @PrePersist
    private void asignarRol() {
        setRol(RolUsuario.SUPER_ADMIN);
    }
}
