package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.EstadoTurno;
import com.example.sitema_de_turnos.modelo.Turno;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class TurnoSpecifications {

    private TurnoSpecifications() {
    }

    public static Specification<Turno> paraCliente(
        Long clienteId,
        EstadoTurno estado,
        Long servicioId,
        LocalDate fechaDesde,
        LocalDate fechaHasta
    ) {
        return Specification.where(conRelaciones())
            .and(clienteIdIgual(clienteId))
            .and(estadoIgual(estado))
            .and(servicioIdIgual(servicioId))
            .and(fechaEnRango(fechaDesde, fechaHasta));
    }

    public static Specification<Turno> paraProfesional(
        Long profesionalId,
        EstadoTurno estado,
        Long servicioId,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        String clienteNombre
    ) {
        return Specification.where(conRelaciones())
            .and(profesionalIdIgual(profesionalId))
            .and(estadoIgual(estado))
            .and(servicioIdIgual(servicioId))
            .and(fechaEnRango(fechaDesde, fechaHasta))
            .and(clienteNombreContiene(clienteNombre));
    }

    private static Specification<Turno> conRelaciones() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("servicio", JoinType.LEFT);
                var profesionalFetch = root.fetch("profesional", JoinType.LEFT);
                profesionalFetch.fetch("usuario", JoinType.LEFT);
                root.fetch("cliente", JoinType.LEFT);
                root.fetch("empresa", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }

    private static Specification<Turno> clienteIdIgual(Long clienteId) {
        return (root, query, cb) -> {
            if (clienteId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("cliente").get("id"), clienteId);
        };
    }

    private static Specification<Turno> profesionalIdIgual(Long profesionalId) {
        return (root, query, cb) -> {
            if (profesionalId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("profesional").get("id"), profesionalId);
        };
    }

    private static Specification<Turno> estadoIgual(EstadoTurno estado) {
        return (root, query, cb) -> {
            if (estado == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("estado"), estado);
        };
    }

    private static Specification<Turno> servicioIdIgual(Long servicioId) {
        return (root, query, cb) -> {
            if (servicioId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("servicio").get("id"), servicioId);
        };
    }

    private static Specification<Turno> fechaEnRango(LocalDate fechaDesde, LocalDate fechaHasta) {
        return (root, query, cb) -> {
            if (fechaDesde != null && fechaHasta != null) {
                return cb.between(root.get("fecha"), fechaDesde, fechaHasta);
            }
            if (fechaDesde != null) {
                return cb.greaterThanOrEqualTo(root.get("fecha"), fechaDesde);
            }
            if (fechaHasta != null) {
                return cb.lessThanOrEqualTo(root.get("fecha"), fechaHasta);
            }
            return cb.conjunction();
        };
    }

    private static Specification<Turno> clienteNombreContiene(String clienteNombre) {
        return (root, query, cb) -> {
            if (clienteNombre == null || clienteNombre.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(
                cb.lower(root.get("cliente").get("nombre")),
                "%" + clienteNombre.trim().toLowerCase() + "%"
            );
        };
    }
}