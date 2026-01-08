package com.example.sitema_de_turnos.dto;

import com.example.sitema_de_turnos.modelo.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private RolUsuario rol;
    private String token; // Para JWT si lo implementas
    
    public LoginResponse(Long id, String nombre, String apellido, String email, RolUsuario rol) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.rol = rol;
    }
}
