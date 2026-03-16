package com.example.sitema_de_turnos.configuracion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Garantiza que el índice único parcial anti-solapamiento existe en la BD
 * al inicio de la aplicación.
 *
 * === ¿Por qué un ApplicationRunner y no Flyway / ddl-auto? ===
 * Los índices únicos PARCIALES (con cláusula WHERE) no son expresables
 * en anotaciones JPA (@UniqueConstraint, @Table). Solo existen como DDL
 * nativo de PostgreSQL. Este runner ejecuta la sentencia de forma idempotente
 * (IF NOT EXISTS) después de que Hibernate haya creado/actualizado la tabla,
 * sin alterar el flujo existente de ddl-auto=update.
 *
 * === Mecanismo de concurrencia que habilita ===
 * El índice bloquea inserciones duplicadas a nivel de motor de base de datos:
 *
 *   CREATE UNIQUE INDEX idx_turno_no_overlap ON turnos (profesional_id, fecha, hora_inicio)
 *   WHERE estado IN ('CONFIRMADO', 'PENDIENTE_PAGO')
 *
 * Flujo en una race condition:
 *   Thread A: SELECT → slot libre → INSERT  ← gana, commit OK
 *   Thread B: SELECT → slot libre (lee antes del commit de A) → INSERT
 *             → PostgreSQL viola el índice → DataIntegrityViolationException
 *             → ServicioTurno lo intercepta → SolapamientoException → HTTP 409
 *
 * El índice es PARCIAL (ignora estados terminales) de modo que, tras cancelar
 * un turno, el mismo slot vuelve a estar disponible para reservar.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IndicesConcurrenciaConfig implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    private static final String INDEX_NAME = "idx_turno_no_overlap";
    private static final List<String> ESTADOS_OCUPANTES = List.of("CONFIRMADO", "PENDIENTE_PAGO");

    @Override
    public void run(ApplicationArguments args) {
        crearIndiceNoSolapamiento();
    }

    private void crearIndiceNoSolapamiento() {
        log.info("Creando/Verificando índice único de concurrencia en BD...");
        try {
            String indexDef = jdbcTemplate.query(
                "SELECT indexdef FROM pg_indexes WHERE schemaname = current_schema() AND indexname = ?",
                rs -> rs.next() ? rs.getString("indexdef") : null,
                INDEX_NAME
            );

            if (indexDef != null && requiereRecreacion(indexDef)) {
                log.warn("Índice '{}' desactualizado. Se recreará con predicado de estados ocupantes explícitos.", INDEX_NAME);
                jdbcTemplate.execute("DROP INDEX IF EXISTS " + INDEX_NAME);
            }

            jdbcTemplate.execute(
                "CREATE UNIQUE INDEX IF NOT EXISTS " + INDEX_NAME + " " +
                "ON turnos (profesional_id, fecha, hora_inicio) " +
                "WHERE estado IN ('CONFIRMADO', 'PENDIENTE_PAGO')"
            );
            log.info("✅ Índice anti-solapamiento '{}' verificado/creado en la BD", INDEX_NAME);
        } catch (Exception e) {
            log.error("❌ No se pudo crear/verificar el índice '{}' de concurrencia. La aplicación no puede iniciar sin esta protección.",
                    INDEX_NAME, e);
            throw new IllegalStateException("No se pudo crear/verificar el índice de concurrencia '" + INDEX_NAME + "'", e);
        }
    }

    private boolean requiereRecreacion(String indexDef) {
        String indexDefUpper = indexDef.toUpperCase();
        boolean contieneEstadosOcupantes = ESTADOS_OCUPANTES.stream().allMatch(indexDefUpper::contains);
        boolean usaPredicadoNotIn = indexDefUpper.contains("NOT IN");
        return !contieneEstadosOcupantes || usaPredicadoNotIn;
    }
}
