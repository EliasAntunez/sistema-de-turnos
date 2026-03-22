package com.example.sitema_de_turnos.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
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

    @Column(name = "datos_bancarios", columnDefinition = "TEXT")
    private String datosBancarios;

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
     * Horas de anticipación antes del turno para enviar recordatorio.
     * Permite que cada empresa configure cuándo enviar recordatorios.
     * Default: 24 horas (1 día antes del turno)
     * Rango válido: 1-168 horas (1 hora a 7 días)
     */
    @Column(name = "horas_antes_recordatorio")
    private Integer horasAntesRecordatorio = 24;

    /**
     * Indica si la empresa tiene habilitado el envío de recordatorios.
     * Permite desactivar recordatorios sin cambiar la configuración.
     * Default: true (recordatorios activos)
     */
    @Column(name = "enviar_recordatorios")
    private Boolean enviarRecordatorios = true;

    @Column(nullable = false)
    private Boolean activa = true;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "perfil_dueno_id", nullable = false, unique = true)
    private PerfilDueno perfilDueno;

    /**
     * Profesionales de la empresa. Cargados solo para guardia @PreRemove.
     * No exponemos esta colección en la API — usar RepositorioPerfilProfesional.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    private List<PerfilProfesional> perfilesProfesionales;

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

    /**
     * Previene el borrado físico de una empresa si tiene perfiles profesionales
     * asociados (FK empresa_id en perfil_profesionales). Fuerza el uso de
     * cambiarEstadoEmpresa(false) como método de baja seguro.
     */
    @PreRemove
    protected void antesDeEliminar() {
        if (perfilesProfesionales != null && !perfilesProfesionales.isEmpty()) {
            throw new IllegalStateException(
                "No se puede eliminar la empresa '" + nombre + "' porque tiene " +
                perfilesProfesionales.size() + " perfil(es) profesional(es) asociado(s). " +
                "Desactive la empresa con cambiarEstadoEmpresa(false) en lugar de eliminarla."
            );
        }
    }
}
