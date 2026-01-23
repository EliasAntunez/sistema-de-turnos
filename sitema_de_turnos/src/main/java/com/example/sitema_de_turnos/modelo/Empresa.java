package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "empresas",
    indexes = {
        @Index(name = "idx_empresa_slug_activa",
               columnList = "slug, activa")
    },
    check = {
        @CheckConstraint(name = "chk_empresa_buffer_positivo",
                        constraint = "buffer_por_defecto >= 0"),
        @CheckConstraint(name = "chk_empresa_anticipacion_positiva",
                        constraint = "tiempo_minimo_anticipacion_minutos >= 0"),
        @CheckConstraint(name = "chk_empresa_dias_reserva_positivo",
                        constraint = "dias_maximos_reserva > 0")
    }
)
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public Empresa(Long id) {
        this.id = id;
    }

    @Column(nullable = false, length = 200)
    private String nombre;

    /**
     * Slug único para URL pública de reservas.
     * Ejemplo: "peluqueria-elegante" para /reservar/peluqueria-elegante
     */
    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, unique = true, length = 20)
    private String cuit;

    @Column(length = 200)
    private String direccion;

    @Column(length = 100)
    private String ciudad;

    @Column(length = 50)
    private String provincia;

    @Column(length = 20)
    private String telefono;

    @Column(length = 150)
    private String email;

    /**
     * Buffer por defecto entre turnos en minutos.
     * Usado cuando el profesional no tiene buffer personalizado.
     * Default: 10 minutos
     */
    @Column(name = "buffer_por_defecto")
    private Integer bufferPorDefecto = 5;

    /**
     * Tiempo mínimo de anticipación en minutos para reservar un turno.
     * Define cuánto tiempo antes debe hacerse la reserva.
     * Ejemplo: 30 minutos significa que no se pueden reservar turnos que comiencen en menos de 30 minutos.
     * Default: 30 minutos
     */
    @Column(name = "tiempo_minimo_anticipacion_minutos")
    private Integer tiempoMinimoAnticipacionMinutos = 30;

    /**
     * Días máximos hacia adelante que los clientes pueden reservar turnos.
     * Default: 30 días
     */
    @Column(name = "dias_maximos_reserva")
    private Integer diasMaximosReserva = 30;

    /**
     * Zona horaria de la empresa para cálculos de fecha/hora.
     * Usado para validar turnos "hoy", horas límite, etc.
     * Default: America/Argentina/Buenos_Aires (Misiones, Argentina)
     * 
     * Preparado para futuro soporte multi-timezone:
     * - America/Argentina/Cordoba
     * - America/Argentina/Ushuaia
     * - America/Sao_Paulo (Brasil)
     * - etc.
     */
    @Column(name = "timezone", length = 50, nullable = true)
    private String timezone = "America/Argentina/Buenos_Aires";

    /**
     * Horas de anticipación para enviar recordatorios de turnos.
     * Ejemplo: 24 = enviar recordatorio 24 horas antes del turno.
     * Default: 24 horas
     */
    @Column(name = "horas_anticipacion_recordatorio")
    private Integer horasAnticipacionRecordatorio = 24;

    /**
     * Indica si los recordatorios automáticos por WhatsApp están activos.
     * false = no enviar recordatorios (job ignora esta empresa)
     * true = enviar recordatorios según configuración
     * Default: false (opt-in, el dueño debe activarlo explícitamente)
     */
    @Column(name = "activar_recordatorios", nullable = true)
    private Boolean activarRecordatorios = false;

    @Column(nullable = false)
    private Boolean activa = true;

    @OneToOne(optional = false)
    @JoinColumn(name = "dueno_id", nullable = false, unique = true)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.RESTRICT)
    private Dueno dueno;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    private List<Profesional> profesionales = new ArrayList<>();

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
