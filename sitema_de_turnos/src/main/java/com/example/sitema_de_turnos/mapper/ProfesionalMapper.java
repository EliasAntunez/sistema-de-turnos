package com.example.sitema_de_turnos.mapper;

import com.example.sitema_de_turnos.dto.ProfesionalResponse;
import com.example.sitema_de_turnos.modelo.PerfilProfesional;

/**
 * Mapper estático para convertir entidades PerfilProfesional a DTOs.
 */
public class ProfesionalMapper {

    private ProfesionalMapper() {
        // Constructor privado para evitar instanciación
    }

    public static ProfesionalResponse toResponse(PerfilProfesional perfil) {
        if (perfil == null) {
            return null;
        }

        ProfesionalResponse response = new ProfesionalResponse();
        response.setId(perfil.getId());
        response.setNombre(perfil.getUsuario().getNombre());
        response.setApellido(perfil.getUsuario().getApellido());
        response.setEmail(perfil.getUsuario().getEmail());
        response.setTelefono(perfil.getUsuario().getTelefono());
        response.setDescripcion(perfil.getDescripcion());
        response.setEmpresaId(perfil.getEmpresa().getId());
        response.setEmpresaNombre(perfil.getEmpresa().getNombre());
        response.setActivo(perfil.getActivo());
        response.setFechaCreacion(perfil.getUsuario().getFechaCreacion());
        response.setFechaActualizacion(perfil.getUsuario().getFechaActualizacion());
        
        return response;
    }
}
