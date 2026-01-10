package com.example.sitema_de_turnos.mapper;

import com.example.sitema_de_turnos.dto.ServicioResponse;
import com.example.sitema_de_turnos.modelo.Especialidad;
import com.example.sitema_de_turnos.modelo.Servicio;

import java.util.stream.Collectors;

/**
 * Mapper estático para convertir entidades Servicio a DTOs.
 * MEJORA-002: Centraliza la lógica de conversión para evitar duplicación.
 */
public class ServicioMapper {

    private ServicioMapper() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Convierte una entidad Servicio a ServicioResponse DTO.
     * 
     * @param servicio Entidad a convertir
     * @return DTO con los datos del servicio
     */
    public static ServicioResponse toResponse(Servicio servicio) {
        if (servicio == null) {
            return null;
        }

        ServicioResponse response = new ServicioResponse();
        response.setId(servicio.getId());
        response.setNombre(servicio.getNombre());
        response.setDescripcion(servicio.getDescripcion());
        response.setDuracionMinutos(servicio.getDuracionMinutos());
        response.setBufferMinutos(servicio.getBufferMinutos());
        response.setPrecio(servicio.getPrecio());
        response.setEspecialidades(
                servicio.getEspecialidades().stream()
                        .map(Especialidad::getNombre)
                        .collect(Collectors.toSet())
        );
        response.setActivo(servicio.getActivo());
        response.setFechaCreacion(servicio.getFechaCreacion());
        
        return response;
    }
}
