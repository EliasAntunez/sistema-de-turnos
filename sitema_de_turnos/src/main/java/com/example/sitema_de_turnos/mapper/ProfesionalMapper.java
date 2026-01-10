package com.example.sitema_de_turnos.mapper;

import com.example.sitema_de_turnos.dto.ProfesionalResponse;
import com.example.sitema_de_turnos.modelo.Especialidad;
import com.example.sitema_de_turnos.modelo.Profesional;

import java.util.stream.Collectors;

/**
 * Mapper estático para convertir entidades Profesional a DTOs.
 * MEJORA-002: Centraliza la lógica de conversión para evitar duplicación.
 */
public class ProfesionalMapper {

    private ProfesionalMapper() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Convierte una entidad Profesional a ProfesionalResponse DTO.
     * 
     * @param profesional Entidad a convertir
     * @return DTO con los datos del profesional
     */
    public static ProfesionalResponse toResponse(Profesional profesional) {
        if (profesional == null) {
            return null;
        }

        ProfesionalResponse response = new ProfesionalResponse();
        response.setId(profesional.getId());
        response.setNombre(profesional.getNombre());
        response.setApellido(profesional.getApellido());
        response.setEmail(profesional.getEmail());
        response.setTelefono(profesional.getTelefono());
        response.setDescripcion(profesional.getDescripcion());
        response.setEspecialidades(
                profesional.getEspecialidades().stream()
                        .map(Especialidad::getNombre)
                        .collect(Collectors.toList())
        );
        response.setEmpresaId(profesional.getEmpresa().getId());
        response.setEmpresaNombre(profesional.getEmpresa().getNombre());
        response.setActivo(profesional.getActivo());
        response.setFechaCreacion(profesional.getFechaCreacion());
        response.setFechaActualizacion(profesional.getFechaActualizacion());
        
        return response;
    }
}
