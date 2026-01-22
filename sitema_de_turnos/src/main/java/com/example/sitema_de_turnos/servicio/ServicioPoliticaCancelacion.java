package com.example.sitema_de_turnos.servicio;

import com.example.sitema_de_turnos.modelo.Empresa;
import com.example.sitema_de_turnos.modelo.PoliticaCancelacion;
import com.example.sitema_de_turnos.modelo.TipoPoliticaCancelacion;
import com.example.sitema_de_turnos.repositorio.RepositorioPoliticaCancelacion;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import com.example.sitema_de_turnos.modelo.PenalizacionPolitica;
import com.example.sitema_de_turnos.excepcion.ConflictoException;

@Service
@RequiredArgsConstructor
public class ServicioPoliticaCancelacion {
    private final RepositorioPoliticaCancelacion repo;
    private final com.example.sitema_de_turnos.servicio.ServicioEmpresa servicioEmpresa;

    @Transactional(readOnly = true)
    public List<PoliticaCancelacion> obtenerPorEmpresa(Empresa empresa) {
        return repo.findByEmpresa(empresa);
    }
    /**
     * Devuelve todas las políticas activas de una empresa (DTO) usando el slug público
     */
    @Transactional(readOnly = true)
    public java.util.List<com.example.sitema_de_turnos.dto.PoliticaCancelacionResponse> obtenerActivasPorEmpresaSlugDTO(String slug) {
        Empresa empresa = servicioEmpresa.obtenerPorSlug(slug);
        if (empresa == null || !empresa.getActiva()) {
            throw new RuntimeException("Empresa no encontrada o inactiva para el slug: " + slug);
        }
        return obtenerActivasPorEmpresaDTO(empresa);
    }
    // Método utilitario para buscar empresa por slug
    // Este método puede estar en ServicioEmpresa, pero lo agregamos aquí para uso directo
    // Si ya existe en ServicioEmpresa, simplemente llamarlo
    // Si no existe, agregarlo en ServicioEmpresa

    @Transactional(readOnly = true)
    public List<PoliticaCancelacion> obtenerActivasPorEmpresa(Empresa empresa) {
        return repo.findByEmpresaAndActivaTrue(empresa);
    }

    @Transactional(readOnly = true)
    public Optional<PoliticaCancelacion> obtenerActivaPorEmpresaYTipo(Empresa empresa, TipoPoliticaCancelacion tipo) {
        return repo.findByEmpresaAndTipoAndActivaTrue(empresa, tipo);
    }

    @Transactional(readOnly = true)
    public List<PoliticaCancelacion> obtenerActivasOrdenadasPorFecha(Empresa empresa) {
        return repo.findAllActivasByEmpresaOrderByFechaCreacionDesc(empresa);
    }


    /**
     * Guarda una nueva política. Si se marca como activa, desactiva otras activas del mismo tipo para la empresa.
     * Si ya existe una activa igual, lanza excepción.
     */
    @Transactional
    public PoliticaCancelacion guardar(PoliticaCancelacion politica) {
        if (politica.getActiva() != null && politica.getActiva()) {
            // Exclusividad: Si es AMBOS, no puede haber ninguna activa de CANCELACION o INASISTENCIA
            // Si es CANCELACION o INASISTENCIA, no puede haber una activa de AMBOS
            if (politica.getTipo() == com.example.sitema_de_turnos.modelo.TipoPoliticaCancelacion.AMBOS) {
                boolean existeEspecifica = repo.findByEmpresaAndTipoAndActivaTrue(politica.getEmpresa(), TipoPoliticaCancelacion.CANCELACION)
                    .filter(p -> !p.getId().equals(politica.getId()))
                    .isPresent()
                    || repo.findByEmpresaAndTipoAndActivaTrue(politica.getEmpresa(), TipoPoliticaCancelacion.INASISTENCIA)
                    .filter(p -> !p.getId().equals(politica.getId()))
                    .isPresent();
                if (existeEspecifica) {
                    throw new ConflictoException("No se puede activar una política de tipo 'AMBOS' si ya existe una activa de 'CANCELACION' o 'INASISTENCIA'.");
                }
            } else {
                boolean existeAmbos = repo.findByEmpresaAndTipoAndActivaTrue(politica.getEmpresa(), TipoPoliticaCancelacion.AMBOS)
                    .filter(p -> !p.getId().equals(politica.getId()))
                    .isPresent();
                if (existeAmbos) {
                    throw new ConflictoException("No se puede activar una política de tipo '" + politica.getTipo() + "' si ya existe una activa de tipo 'AMBOS'.");
                }
            }
            // Desactivar otra activa del mismo tipo (solo puede haber una activa por tipo)
            repo.findByEmpresaAndTipoAndActivaTrue(politica.getEmpresa(), politica.getTipo())
                .ifPresent(p -> {
                    if (!p.getId().equals(politica.getId())) { // Solo si es distinta
                        p.setActiva(false);
                        repo.save(p);
                    }
                });
        }
        // Validación básica: no permitir penalización MULTA sin descripción
        if (politica.getPenalizacion() == PenalizacionPolitica.MULTA && (politica.getDescripcion() == null || politica.getDescripcion().isBlank())) {
            throw new IllegalArgumentException("Debe especificar la descripción de la multa.");
        }
        try {
            return repo.save(politica);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("uk_politica_empresa_tipo_activa")) {
                throw new ConflictoException("Ya existe una política activa de este tipo para la empresa. Solo puede haber una activa por tipo.");
            }
            throw ex;
        }
    }

    /**
     * Desactiva una política por ID (soft delete).
     */
    @Transactional
    public void desactivar(Long id) {
        PoliticaCancelacion politica = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Política no encontrada"));
        politica.setActiva(false);
        repo.save(politica);
    }

    /**
     * Activa una política por ID y desactiva otras activas del mismo tipo y empresa.
     */
    @Transactional
    public void activar(Long id) {
        PoliticaCancelacion politica = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Política no encontrada"));
        if (Boolean.TRUE.equals(politica.getActiva())) {
            // Ya está activa, no hacer nada
            return;
        }

        // Exclusividad: Si es AMBOS, no puede haber ninguna activa de CANCELACION o INASISTENCIA
        if (politica.getTipo() == TipoPoliticaCancelacion.AMBOS) {
            boolean existeEspecifica = repo.findByEmpresaAndTipoAndActivaTrue(politica.getEmpresa(), TipoPoliticaCancelacion.CANCELACION).isPresent()
                || repo.findByEmpresaAndTipoAndActivaTrue(politica.getEmpresa(), TipoPoliticaCancelacion.INASISTENCIA).isPresent();
            if (existeEspecifica) {
                throw new ConflictoException("No se puede activar una política de tipo 'AMBOS' si ya existe una activa de 'CANCELACION' o 'INASISTENCIA'.");
            }
        } else {
            boolean existeAmbos = repo.findByEmpresaAndTipoAndActivaTrue(politica.getEmpresa(), TipoPoliticaCancelacion.AMBOS).isPresent();
            if (existeAmbos) {
                throw new ConflictoException("No se puede activar una política de tipo '" + politica.getTipo() + "' si ya existe una activa de tipo 'AMBOS'.");
            }
        }

        // Desactivar otras activas del mismo tipo y empresa
        repo.findByEmpresaAndTipoAndActivaTrue(politica.getEmpresa(), politica.getTipo())
            .ifPresent(p -> {
                p.setActiva(false);
                repo.save(p);
            });
        politica.setActiva(true);
        repo.save(politica);
    }

    /**
     * Elimina una política (hard delete).
     */
    @Transactional
    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    // Métodos adaptados para DTO y Mapper
    /**
     * Devuelve todas las políticas de una empresa como DTO
     */
    @Transactional(readOnly = true)
    public java.util.List<com.example.sitema_de_turnos.dto.PoliticaCancelacionResponse> obtenerPorEmpresaDTO(com.example.sitema_de_turnos.modelo.Empresa empresa) {
        return obtenerPorEmpresa(empresa).stream()
            .map(com.example.sitema_de_turnos.mapper.PoliticaCancelacionMapper::toResponse)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Devuelve todas las políticas activas de una empresa como DTO
     */
    @Transactional(readOnly = true)
    public java.util.List<com.example.sitema_de_turnos.dto.PoliticaCancelacionResponse> obtenerActivasPorEmpresaDTO(com.example.sitema_de_turnos.modelo.Empresa empresa) {
        return obtenerActivasPorEmpresa(empresa).stream()
            .map(com.example.sitema_de_turnos.mapper.PoliticaCancelacionMapper::toResponse)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Crea una política desde el DTO y devuelve el DTO de respuesta
     */
    @Transactional
    public com.example.sitema_de_turnos.dto.PoliticaCancelacionResponse crearDesdeDTO(com.example.sitema_de_turnos.dto.PoliticaCancelacionRequest request, com.example.sitema_de_turnos.modelo.Empresa empresa) {
        com.example.sitema_de_turnos.modelo.PoliticaCancelacion entity = com.example.sitema_de_turnos.mapper.PoliticaCancelacionMapper.toEntity(request, empresa);
        com.example.sitema_de_turnos.modelo.PoliticaCancelacion guardada = guardar(entity);
        return com.example.sitema_de_turnos.mapper.PoliticaCancelacionMapper.toResponse(guardada);
    }

    /**
     * Actualiza una política existente desde el DTO y devuelve el DTO de respuesta
     */    
    /**
     * Actualiza una política existente desde el DTO y la asocia SIEMPRE a la empresa del dueño autenticado.
     * El parámetro empresa debe venir del controlador, nunca del request.
     */
    @Transactional
    public com.example.sitema_de_turnos.dto.PoliticaCancelacionResponse actualizarDesdeDTO(Long id, com.example.sitema_de_turnos.dto.PoliticaCancelacionRequest request, com.example.sitema_de_turnos.modelo.Empresa empresa) {
        com.example.sitema_de_turnos.modelo.PoliticaCancelacion entity = com.example.sitema_de_turnos.mapper.PoliticaCancelacionMapper.toEntity(request, empresa);
        entity.setId(id);
        com.example.sitema_de_turnos.modelo.PoliticaCancelacion actualizada = guardar(entity);
        return com.example.sitema_de_turnos.mapper.PoliticaCancelacionMapper.toResponse(actualizada);
    }
}