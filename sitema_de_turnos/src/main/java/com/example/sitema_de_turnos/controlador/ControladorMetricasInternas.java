package com.example.sitema_de_turnos.controlador;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

import com.example.sitema_de_turnos.repositorio.RepositorioTurno;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class ControladorMetricasInternas {

    private final RepositorioTurno repositorioTurno;

    // Spring va a buscar la propiedad. Si no existe, usa lo que esté después de los dos puntos
    @Value("${app.security.internal-token:clave_segura_por_defecto_dev}")
    private String tokenSecreto;

    @GetMapping("/metrics")
    public ResponseEntity<?> obtenerMetricasPorRango(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @RequestParam(value = "rango", defaultValue = "mes") String rango) {

        // 1. Validación manual de seguridad
        if (token == null || !token.equals(tokenSecreto)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("exito", false, "mensaje", "Acceso denegado: Token inválido"));
        }

        // 2. Calcular la fecha hacia atrás
        LocalDate fechaDesde = LocalDate.now();
        if ("semana".equalsIgnoreCase(rango)) {
            fechaDesde = fechaDesde.minusDays(7);
        } else {
            fechaDesde = fechaDesde.minusMonths(1);
        }

        // 3. Definir estados facturables (Excluímos los CANCELADOS por salud del negocio)
        List<com.example.sitema_de_turnos.modelo.EstadoTurno> estadosValidos = List.of(
                com.example.sitema_de_turnos.modelo.EstadoTurno.CONFIRMADO,
                com.example.sitema_de_turnos.modelo.EstadoTurno.ATENDIDO
        );

        // 4. Consultar la Base de Datos
        List<Map<String, Object>> rawDatos = repositorioTurno.countTurnosPorEmpresaDesde(fechaDesde, estadosValidos);

        // 5. Mapear y embellecer las llaves para la consola (Evita el problema de minúsculas de Postgres)
        List<Map<String, Object>> metricasPerfiladas = rawDatos.stream().map(row -> {
            // Buscamos la llave tanto en minúscula como en CamelCase por seguridad del driver
            Object nombre = row.get("nombreempresa") != null ? row.get("nombreempresa") : row.get("nombreEmpresa");
            Object turnos = row.get("totalturnos") != null ? row.get("totalturnos") : row.get("totalTurnos");
            
            return Map.of(
                "empresa", nombre != null ? nombre : "Sin Nombre",
                "turnos", turnos != null ? turnos : 0
            );
        }).toList();

        // 6. Retornar respuesta impecable
        return ResponseEntity.ok(Map.of(
                "exito", true,
                "rango_evaluado", rango.toLowerCase(),
                "desde", fechaDesde,
                "hasta", LocalDate.now(),
                "empresas_activas", metricasPerfiladas.size(),
                "ranking", metricasPerfiladas
        ));
    }
}