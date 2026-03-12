package com.example.sitema_de_turnos.repositorio;

import com.example.sitema_de_turnos.modelo.Notificacion;
import com.example.sitema_de_turnos.modelo.PerfilProfesional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioNotificacion extends JpaRepository<Notificacion, Long> {

    /**
     * Obtener todas las notificaciones no leídas de un profesional
     * Ordenadas por fecha de creación descendente (más recientes primero)
     */
    @Query("SELECT n FROM Notificacion n WHERE n.profesional = :profesional AND n.leida = false ORDER BY n.fechaCreacion DESC")
    List<Notificacion> findNoLeidasByProfesional(@Param("profesional") PerfilProfesional profesional);

    /**
     * Contar notificaciones no leídas de un profesional
     */
    Long countByProfesionalAndLeidaFalse(PerfilProfesional profesional);

    /**
     * Obtener todas las notificaciones de un profesional con paginación
     * Ordenadas por fecha de creación descendente
     */
    @Query("SELECT n FROM Notificacion n WHERE n.profesional = :profesional ORDER BY n.fechaCreacion DESC")
    Page<Notificacion> findByProfesionalOrderByFechaCreacionDesc(
        @Param("profesional") PerfilProfesional profesional, 
        Pageable pageable
    );

    /**
     * Obtener las últimas N notificaciones de un profesional
     */
    @Query("SELECT n FROM Notificacion n WHERE n.profesional = :profesional ORDER BY n.fechaCreacion DESC")
    List<Notificacion> findTopNByProfesional(@Param("profesional") PerfilProfesional profesional, Pageable pageable);

    /**
     * ✅ U1 + M7: Eliminar notificaciones leídas antiguas (cleanup)
     * Usado por scheduler mensual para mantener la BD limpia
     * @param fechaLimite Notificaciones leídas antes de esta fecha serán eliminadas
     * @return Número de notificaciones eliminadas
     */
    @Modifying
    @Query("DELETE FROM Notificacion n WHERE n.leida = true AND n.fechaLectura < :fechaLimite")
    int deleteNotificacionesLeidasAntiguas(@Param("fechaLimite") java.time.LocalDateTime fechaLimite);
}
