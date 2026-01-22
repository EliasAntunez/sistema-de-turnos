package com.example.sitema_de_turnos.mapper;

import com.example.sitema_de_turnos.dto.PoliticaCancelacionRequest;
import com.example.sitema_de_turnos.dto.PoliticaCancelacionResponse;
import com.example.sitema_de_turnos.modelo.PoliticaCancelacion;
import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.TipoPoliticaCancelacion;
import com.example.sitema_de_turnos.modelo.PenalizacionPolitica;

public class PoliticaCancelacionMapper {
    public static PoliticaCancelacion toEntity(PoliticaCancelacionRequest dto, Empresa empresa) {
        PoliticaCancelacion entity = new PoliticaCancelacion();
        entity.setEmpresa(empresa); // SIEMPRE la empresa pasada por parámetro
        entity.setTipo(TipoPoliticaCancelacion.valueOf(dto.getTipo()));
        entity.setDescripcion(dto.getDescripcion());
        entity.setHorasLimiteCancelacion(dto.getHorasLimiteCancelacion());
        entity.setPenalizacion(PenalizacionPolitica.valueOf(dto.getPenalizacion()));
        entity.setMensajeCliente(dto.getMensajeCliente());
        entity.setActiva(dto.isActiva());
        return entity;
    }

    public static PoliticaCancelacionResponse toResponse(PoliticaCancelacion entity) {
        PoliticaCancelacionResponse dto = new PoliticaCancelacionResponse();
        dto.setId(entity.getId());
        dto.setEmpresaId(entity.getEmpresa().getId());
        dto.setTipo(entity.getTipo().name());
        dto.setDescripcion(entity.getDescripcion());
        dto.setHorasLimiteCancelacion(entity.getHorasLimiteCancelacion());
        dto.setPenalizacion(entity.getPenalizacion().name());
        dto.setMensajeCliente(entity.getMensajeCliente());
        dto.setActiva(entity.getActiva());
        dto.setFechaCreacion(entity.getFechaCreacion() != null ? entity.getFechaCreacion().toString() : null);
        dto.setFechaModificacion(entity.getFechaModificacion() != null ? entity.getFechaModificacion().toString() : null);
        return dto;
    }
}
