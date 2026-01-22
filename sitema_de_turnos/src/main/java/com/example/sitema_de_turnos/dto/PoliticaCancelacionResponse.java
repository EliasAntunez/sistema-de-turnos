package com.example.sitema_de_turnos.dto;

public class PoliticaCancelacionResponse {
    private Long id;
    private Long empresaId;
    private String tipo;
    private String descripcion;
    private int horasLimiteCancelacion;
    private String penalizacion;
    private String mensajeCliente;
    private boolean activa;
    private String fechaCreacion;
    private String fechaModificacion;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getHorasLimiteCancelacion() { return horasLimiteCancelacion; }
    public void setHorasLimiteCancelacion(int horasLimiteCancelacion) { this.horasLimiteCancelacion = horasLimiteCancelacion; }

    public String getPenalizacion() { return penalizacion; }
    public void setPenalizacion(String penalizacion) { this.penalizacion = penalizacion; }


    public String getMensajeCliente() { return mensajeCliente; }
    public void setMensajeCliente(String mensajeCliente) { this.mensajeCliente = mensajeCliente; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(String fechaModificacion) { this.fechaModificacion = fechaModificacion; }
}
