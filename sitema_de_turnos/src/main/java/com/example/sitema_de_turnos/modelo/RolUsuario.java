package com.example.sitema_de_turnos.modelo;

public enum RolUsuario {
    SUPER_ADMIN("Super Administrador"),
    DUENO("Dueño de Empresa"),
    PROFESIONAL("Profesional/Empleado"),
    CLIENTE("Cliente");

    private final String descripcion;

    RolUsuario(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
